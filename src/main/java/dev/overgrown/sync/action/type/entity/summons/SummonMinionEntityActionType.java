package dev.overgrown.sync.action.type.entity.summons;

import dev.overgrown.sync.power.type.summons.entities.minion.MinionEntity;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import dev.overgrown.sync.registry.SyncEntityRegistry;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SummonMinionEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SummonMinionEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("texture", SerializableDataTypes.IDENTIFIER, MinionEntity.TEMPLATE_TEXTURE)
            .add("follow_owner", SerializableDataTypes.BOOLEAN)
            .add("follow_offset", SerializableDataTypes.VECTOR.optional(), Optional.empty())
            .add("scale", SerializableDataTypes.FLOAT, 1f)
            .add("invulnerable", SerializableDataTypes.BOOLEAN, false)
            .add("max_life_ticks", SerializableDataTypes.INT, 1200)
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new SummonMinionEntityActionType(
            data.get("texture"),
            data.get("follow_owner"),
            data.get("follow_offset"),
            data.get("scale"),
            data.get("invulnerable"),
            data.get("max_life_ticks"),
            data.get("bientity_action")
        ),
        (a, sd) -> sd.instance()
            .set("texture", a.texture)
            .set("follow_owner", a.shouldFollow)
            .set("follow_offset", a.offset)
            .set("scale", a.scale)
            .set("invulnerable", a.isInvulnerable)
            .set("max_life_ticks", a.maxLife)
            .set("bientity_action", a.bientityAction)
    );

    private final Identifier texture;
    private final boolean shouldFollow;
    private final Optional<Vec3d> offset;
    private final float scale;
    private final boolean isInvulnerable;
    private final int maxLife;
    private final Optional<BiEntityAction> bientityAction;

    public SummonMinionEntityActionType(Identifier texture, boolean shouldFollow, Optional<Vec3d> offset,
                                        float scale, boolean isInvulnerable, int maxLife,
                                        Optional<BiEntityAction> bientityAction) {
        this.texture = texture;
        this.shouldFollow = shouldFollow;
        this.offset = offset;
        this.scale = scale;
        this.isInvulnerable = isInvulnerable;
        this.maxLife = maxLife;
        this.bientityAction = bientityAction;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity() instanceof LivingEntity living && living.getWorld() instanceof ServerWorld world) {
            MinionEntity minion = new MinionEntity(SyncEntityRegistry.MINION, world);
            minion.setOwner(living);
            minion.setTexture(texture);
            minion.setFollowOwner(shouldFollow);
            minion.setScale(scale);
            minion.setInvulnerable(isInvulnerable);

            Vec3d minionPosition = new Vec3d(living.getX(), living.getY(), living.getZ());

            if (offset.isPresent()) {
                if (shouldFollow) minion.setFollowOwnerOffset(offset.get());
                minionPosition = minionPosition.add(offset.get());
            }

            minion.refreshPositionAndAngles(minionPosition.getX(), minionPosition.getY(), minionPosition.getZ(),
                living.getHeadYaw(), living.getPitch());
            minion.initialize(world, world.getLocalDifficulty(living.getBlockPos()), SpawnReason.REINFORCEMENT, null);
            minion.setCustomName(Text.of("Minion of " + living.getName().getString()));
            minion.setMaxLifetime(maxLife);

            living.getWorld().spawnEntity(minion);

            bientityAction.ifPresent(a -> a.accept(new BiEntityActionContext(living, minion)));
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.SUMMON_MINION;
    }
}
