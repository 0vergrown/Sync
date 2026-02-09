package dev.overgrown.sync.factory.action.entity.summons;

import java.util.function.Consumer;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.entities.minion.MinionEntity;
import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

public class SummonMinionAction {
    public static void action (SerializableData.Instance data, Entity entity) {
        if (entity instanceof LivingEntity living && living.getWorld() instanceof ServerWorld world) {
            final Identifier texture = data.get("texture");
            final boolean shouldFollow = data.getBoolean("follow_owner");
            final Vec3d offset = data.get("follow_offset");
            final float scale = data.getFloat("scale");
            final boolean isInvulnerable = data.getBoolean("invulnerable");
            final int maxLife = data.getInt("max_life_ticks");
            final Consumer<Pair<Entity, Entity>> bientityAction = data.get("bientity_action");

            MinionEntity minion = new MinionEntity(SyncEntityRegistry.MINION, world);
            minion.setOwner(living);
            minion.setTexture(texture);
            minion.setFollowOwner(shouldFollow);
            minion.setScale(scale);
            minion.setInvulnerable(isInvulnerable);

            Vec3d minionPosition = new Vec3d(living.getX(), living.getY(), living.getZ());

            if (offset != null) {
                if (shouldFollow) minion.setFollowOwnerOffset(offset);
                minionPosition = minionPosition.add(offset);
            }

            minion.refreshPositionAndAngles(minionPosition.getX(), minionPosition.getY(), minionPosition.getZ(), living.getHeadYaw(), living.getPitch());
            minion.initialize(world, world.getLocalDifficulty(living.getBlockPos()), SpawnReason.REINFORCEMENT, null, null);
            minion.setCustomName(Text.of("Minion of " + entity.getName().getString()));
            minion.setMaxLifetime(maxLife);

            living.getWorld().spawnEntity(minion);

            // Minion is now in the world, actions can be performed on it.
            if (bientityAction != null) bientityAction.accept(new Pair<>(living, minion));
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Sync.identifier("summon_minion"),
                new SerializableData()
                        .add("texture", SerializableDataTypes.IDENTIFIER, MinionEntity.TEMPLATE_TEXTURE)
                        .add("follow_owner", SerializableDataTypes.BOOLEAN)
                        .add("follow_offset", SerializableDataTypes.VECTOR, null)
                        .add("scale", SerializableDataTypes.FLOAT, 1f)
                        .add("invulnerable", SerializableDataTypes.BOOLEAN, false)
                        .add("max_life_ticks", SerializableDataTypes.INT, 1200)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
                SummonMinionAction::action
        );
    }
}