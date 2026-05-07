package dev.overgrown.sync.action.type.entity.custom_projectile;

import dev.overgrown.sync.power.type.custom_projectile.entities.CustomProjectileEntity;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomProjectileEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<CustomProjectileEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_id", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("texture_location", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("count", SerializableDataTypes.INT, 1)
            .add("speed", SerializableDataTypes.FLOAT, 1.5F)
            .add("divergence", SerializableDataTypes.FLOAT, 1.0F)
            .add("sound", SerializableDataTypes.SOUND_EVENT.optional(), Optional.empty())
            .add("tag", SerializableDataTypes.NBT_COMPOUND.optional(), Optional.empty())
            .add("entity_action_before_firing", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action_after_firing", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action_on_hit", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action_on_miss", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action_on_hit", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("owner_target_bientity_action_on_hit", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action_cancels_miss_action", SerializableDataTypes.BOOLEAN, false)
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("owner_bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("tick_bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new CustomProjectileEntityActionType(
            data.get("entity_id"),
            data.get("texture_location"),
            data.get("count"),
            data.get("speed"),
            data.get("divergence"),
            data.get("sound"),
            data.get("tag"),
            data.get("entity_action_before_firing"),
            data.get("bientity_action_after_firing"),
            data.get("block_action_on_hit"),
            data.get("bientity_action_on_miss"),
            data.get("bientity_action_on_hit"),
            data.get("owner_target_bientity_action_on_hit"),
            data.get("block_action_cancels_miss_action"),
            data.get("block_condition"),
            data.get("bientity_condition"),
            data.get("owner_bientity_condition"),
            data.get("tick_bientity_action")
        ),
        (a, sd) -> sd.instance()
            .set("entity_id", a.entityId)
            .set("texture_location", a.textureLocation)
            .set("count", a.count)
            .set("speed", a.speed)
            .set("divergence", a.divergence)
            .set("sound", a.sound)
            .set("tag", a.tag)
            .set("entity_action_before_firing", a.entityActionBeforeFiring)
            .set("bientity_action_after_firing", a.bientityActionAfterFiring)
            .set("block_action_on_hit", a.blockActionOnHit)
            .set("bientity_action_on_miss", a.bientityActionOnMiss)
            .set("bientity_action_on_hit", a.bientityActionOnHit)
            .set("owner_target_bientity_action_on_hit", a.ownerTargetBientityActionOnHit)
            .set("block_action_cancels_miss_action", a.blockActionCancelsMissAction)
            .set("block_condition", a.blockCondition)
            .set("bientity_condition", a.bientityCondition)
            .set("owner_bientity_condition", a.ownerBientityCondition)
            .set("tick_bientity_action", a.tickBientityAction)
    );

    private final Optional<Identifier> entityId;
    private final Optional<Identifier> textureLocation;
    private final int count;
    private final float speed;
    private final float divergence;
    private final Optional<SoundEvent> sound;
    private final Optional<NbtCompound> tag;
    private final Optional<EntityAction> entityActionBeforeFiring;
    private final Optional<BiEntityAction> bientityActionAfterFiring;
    private final Optional<BlockAction> blockActionOnHit;
    private final Optional<BiEntityAction> bientityActionOnMiss;
    private final Optional<BiEntityAction> bientityActionOnHit;
    private final Optional<BiEntityAction> ownerTargetBientityActionOnHit;
    private final boolean blockActionCancelsMissAction;
    private final Optional<BlockCondition> blockCondition;
    private final Optional<BiEntityCondition> bientityCondition;
    private final Optional<BiEntityCondition> ownerBientityCondition;
    private final Optional<BiEntityAction> tickBientityAction;

    public CustomProjectileEntityActionType(Optional<Identifier> entityId, Optional<Identifier> textureLocation,
                                            int count, float speed, float divergence,
                                            Optional<SoundEvent> sound, Optional<NbtCompound> tag,
                                            Optional<EntityAction> entityActionBeforeFiring,
                                            Optional<BiEntityAction> bientityActionAfterFiring,
                                            Optional<BlockAction> blockActionOnHit,
                                            Optional<BiEntityAction> bientityActionOnMiss,
                                            Optional<BiEntityAction> bientityActionOnHit,
                                            Optional<BiEntityAction> ownerTargetBientityActionOnHit,
                                            boolean blockActionCancelsMissAction,
                                            Optional<BlockCondition> blockCondition,
                                            Optional<BiEntityCondition> bientityCondition,
                                            Optional<BiEntityCondition> ownerBientityCondition,
                                            Optional<BiEntityAction> tickBientityAction) {
        this.entityId = entityId;
        this.textureLocation = textureLocation;
        this.count = count;
        this.speed = speed;
        this.divergence = divergence;
        this.sound = sound;
        this.tag = tag;
        this.entityActionBeforeFiring = entityActionBeforeFiring;
        this.bientityActionAfterFiring = bientityActionAfterFiring;
        this.blockActionOnHit = blockActionOnHit;
        this.bientityActionOnMiss = bientityActionOnMiss;
        this.bientityActionOnHit = bientityActionOnHit;
        this.ownerTargetBientityActionOnHit = ownerTargetBientityActionOnHit;
        this.blockActionCancelsMissAction = blockActionCancelsMissAction;
        this.blockCondition = blockCondition;
        this.bientityCondition = bientityCondition;
        this.ownerBientityCondition = ownerBientityCondition;
        this.tickBientityAction = tickBientityAction;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        if (entity == null || entity.getWorld().isClient() || !(entity instanceof LivingEntity living)) return;

        entityActionBeforeFiring.ifPresent(a -> a.accept(new EntityActionContext(living)));

        sound.ifPresent(s -> living.getWorld().playSound(null,
            living.getX(), living.getY(), living.getZ(), s, SoundCategory.NEUTRAL,
            0.5F, 0.4F / (living.getRandom().nextFloat() * 0.4F + 0.8F)));

        for (int i = 0; i < count; i++) {
            Vec3d rotationVec = living.getRotationVector();
            Vec3d spawnPos = new Vec3d(living.getX(), living.getEyeY(), living.getZ()).add(rotationVec);

            CustomProjectileEntity projectile = new CustomProjectileEntity(
                spawnPos.x, spawnPos.y, spawnPos.z, living, living.getWorld());

            float adjustedDivergence = (i == 0) ? 0.0F : divergence * 0.075F;
            projectile.setVelocity(living, living.getPitch(), living.getYaw(), 0.0F, speed, adjustedDivergence);

            entityId.ifPresent(projectile::setEntityId);
            textureLocation.ifPresent(projectile::setTextureLocation);

            if (tag.isPresent()) {
                NbtCompound merged = projectile.writeNbt(new NbtCompound());
                merged.copyFrom(tag.get());
                projectile.readNbt(merged);
            }

            blockActionOnHit.ifPresent(projectile::setBlockAction);
            bientityActionOnMiss.ifPresent(projectile::setMissBiEntityAction);
            bientityActionOnHit.ifPresent(projectile::setImpactBiEntityAction);
            ownerTargetBientityActionOnHit.ifPresent(projectile::setOwnerImpactBiEntityAction);
            tickBientityAction.ifPresent(projectile::setTickBiEntityAction);
            projectile.setBlockActionCancelsMissAction(blockActionCancelsMissAction);
            blockCondition.ifPresent(projectile::setBlockCondition);
            bientityCondition.ifPresent(projectile::setBiEntityCondition);
            ownerBientityCondition.ifPresent(projectile::setOwnerBiEntityCondition);

            living.getWorld().spawnEntity(projectile);

            bientityActionAfterFiring.ifPresent(a -> a.accept(new BiEntityActionContext(living, projectile)));
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.CUSTOM_PROJECTILE;
    }
}
