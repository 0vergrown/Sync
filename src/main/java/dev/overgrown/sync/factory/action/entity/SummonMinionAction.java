package dev.overgrown.sync.factory.action.entity;

import java.util.function.Consumer;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.entities.minion.MinionEntity;
import dev.overgrown.sync.entities.registry.SyncEntityRegistry;
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
    private static final String TEXTURE_LABEL = "texture";
    private static final String FOLLOW_OWNER_LABEL = "follow_owner";
    private static final String FOLLOW_OWNER_OFFSET_LABEL = "follow_offset";
    private static final String SCALE_LABEL = "scale";
    private static final String INVULNERABLE_LABEL = "invulnerable";
    private static final String LIFE_LABEL = "max_life_ticks";
    private static final String BIENTITY_ACTION_LABEL = "bientity_action";

    public static void action (SerializableData.Instance data, Entity entity) {
        if (entity instanceof LivingEntity living && living.getWorld() instanceof ServerWorld world) {
            final Identifier texture = data.get(TEXTURE_LABEL);
            final boolean shouldFollow = data.getBoolean(FOLLOW_OWNER_LABEL);
            final Vec3d offset = data.get(FOLLOW_OWNER_OFFSET_LABEL);
            final float scale = data.getFloat(SCALE_LABEL);
            final boolean isInvulnerable = data.getBoolean(INVULNERABLE_LABEL);
            final int maxLife = data.getInt(LIFE_LABEL);

            final Consumer<Pair<Entity, Entity>> bientityAction = data.get(BIENTITY_ACTION_LABEL);

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
                        .add(TEXTURE_LABEL, SerializableDataTypes.IDENTIFIER, MinionEntity.TEMPLATE_TEXTURE)
                        .add(FOLLOW_OWNER_LABEL, SerializableDataTypes.BOOLEAN)
                        .add(FOLLOW_OWNER_OFFSET_LABEL, SerializableDataTypes.VECTOR, null)
                        .add(SCALE_LABEL, SerializableDataTypes.FLOAT, 1f)
                        .add(INVULNERABLE_LABEL, SerializableDataTypes.BOOLEAN, false)
                        .add(LIFE_LABEL, SerializableDataTypes.INT, 1200)
                        .add(BIENTITY_ACTION_LABEL, ApoliDataTypes.BIENTITY_ACTION, null),
                SummonMinionAction::action);
    }
}