package dev.overgrown.sync.factory.power.type;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.entities.custom_projectile.CustomProjectileEntity;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class CustomProjectilePower extends CooldownPower implements Active {
    private Active.Key key;
    private int shotProjectiles;
    private boolean finishedStartDelay;
    private boolean isFiringProjectiles;

    private final Identifier textureLocation;
    private final int count;
    private final int interval;
    private final int startDelay;
    private final float speed;
    private final float divergence;
    private final SoundEvent sound;
    private final NbtCompound tag;
    private final boolean allowConditionalCancelling;
    private final boolean blockActionCancelsMissAction;

    private final ActionFactory<Entity>.Instance entityActionBeforeFiring;
    private final ActionFactory<Pair<Entity, Entity>>.Instance bientityActionAfterFiring;
    private final ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockActionOnHit;
    private final ActionFactory<Pair<Entity, Entity>>.Instance bientityActionOnMiss;
    private final ActionFactory<Pair<Entity, Entity>>.Instance bientityActionOnHit;
    private final ActionFactory<Pair<Entity, Entity>>.Instance ownerTargetBientityActionOnHit;
    private final ActionFactory<Pair<Entity, Entity>>.Instance tickBientityAction;

    private final ConditionFactory<CachedBlockPosition>.Instance blockCondition;
    private final ConditionFactory<Pair<Entity, Entity>>.Instance bientityCondition;
    private final ConditionFactory<Pair<Entity, Entity>>.Instance ownerBientityCondition;

    public CustomProjectilePower(
            PowerType<?> type, LivingEntity entity,
            int cooldownDuration, HudRender hudRender,
            Identifier textureLocation, int count, int interval, int startDelay,
            float speed, float divergence, SoundEvent sound, NbtCompound tag,
            boolean allowConditionalCancelling, boolean blockActionCancelsMissAction,
            ActionFactory<Entity>.Instance entityActionBeforeFiring,
            ActionFactory<Pair<Entity, Entity>>.Instance bientityActionAfterFiring,
            ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockActionOnHit,
            ActionFactory<Pair<Entity, Entity>>.Instance bientityActionOnMiss,
            ActionFactory<Pair<Entity, Entity>>.Instance bientityActionOnHit,
            ActionFactory<Pair<Entity, Entity>>.Instance ownerTargetBientityActionOnHit,
            ActionFactory<Pair<Entity, Entity>>.Instance tickBientityAction,
            ConditionFactory<CachedBlockPosition>.Instance blockCondition,
            ConditionFactory<Pair<Entity, Entity>>.Instance bientityCondition,
            ConditionFactory<Pair<Entity, Entity>>.Instance ownerBientityCondition
    ) {
        super(type, entity, cooldownDuration, hudRender);
        this.textureLocation = textureLocation;
        this.count = count;
        this.interval = interval;
        this.startDelay = startDelay;
        this.speed = speed;
        this.divergence = divergence;
        this.sound = sound;
        this.tag = tag;
        this.allowConditionalCancelling = allowConditionalCancelling;
        this.blockActionCancelsMissAction = blockActionCancelsMissAction;
        this.entityActionBeforeFiring = entityActionBeforeFiring;
        this.bientityActionAfterFiring = bientityActionAfterFiring;
        this.blockActionOnHit = blockActionOnHit;
        this.bientityActionOnMiss = bientityActionOnMiss;
        this.bientityActionOnHit = bientityActionOnHit;
        this.ownerTargetBientityActionOnHit = ownerTargetBientityActionOnHit;
        this.tickBientityAction = tickBientityAction;
        this.blockCondition = blockCondition;
        this.bientityCondition = bientityCondition;
        this.ownerBientityCondition = ownerBientityCondition;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        if (canUse()) {
            isFiringProjectiles = true;
            use();
        }
    }

    @Override
    public void tick() {
        if (!isFiringProjectiles) return;

        if (!finishedStartDelay && startDelay == 0) {
            finishedStartDelay = true;
        }

        long timeSinceUse = entity.getEntityWorld().getTime() - lastUseTime;

        if (!finishedStartDelay && timeSinceUse % startDelay == 0) {
            finishedStartDelay = true;
            shotProjectiles++;
            if (shotProjectiles <= count) {
                playSound();
                if (!entity.getWorld().isClient) {
                    fireProjectile();
                }
            } else {
                reset();
            }
        } else if (interval == 0 && finishedStartDelay) {
            playSound();
            if (!entity.getWorld().isClient) {
                while (shotProjectiles < count) {
                    fireProjectile();
                    shotProjectiles++;
                }
            }
            reset();
        } else if (finishedStartDelay && timeSinceUse % interval == 0) {
            shotProjectiles++;
            if (shotProjectiles <= count) {
                playSound();
                if (!entity.getWorld().isClient) {
                    fireProjectile();
                }
            } else {
                reset();
            }
        }
    }

    private void playSound() {
        if (sound != null) {
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    sound, SoundCategory.NEUTRAL, 0.5F,
                    0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    private void reset() {
        shotProjectiles = 0;
        finishedStartDelay = false;
        isFiringProjectiles = false;
    }

    private void fireProjectile() {
        if (entityActionBeforeFiring != null) {
            entityActionBeforeFiring.accept(entity);
        }

        if (allowConditionalCancelling && !isActive()) {
            isFiringProjectiles = false;
            return;
        }

        Vec3d rotationVec = entity.getRotationVector();
        Vec3d spawnPos = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ()).add(rotationVec);

        CustomProjectileEntity projectile = new CustomProjectileEntity(
                spawnPos.x, spawnPos.y, spawnPos.z, entity, entity.getWorld());

        projectile.setVelocity(entity, entity.getPitch(), entity.getYaw(), 0.0F, speed, divergence * 0.075F);
        projectile.setEntityId(type.getIdentifier());
        projectile.setTextureLocation(textureLocation);
        projectile.setBlockAction(blockActionOnHit);
        projectile.setBlockActionCancelsMissAction(blockActionCancelsMissAction);
        projectile.setMissBiEntityAction(bientityActionOnMiss);
        projectile.setImpactBiEntityAction(bientityActionOnHit);
        projectile.setOwnerImpactBiEntityAction(ownerTargetBientityActionOnHit);
        projectile.setBlockCondition(blockCondition);
        projectile.setBiEntityCondition(bientityCondition);
        projectile.setOwnerBiEntityCondition(ownerBientityCondition);
        projectile.setTickBiEntityAction(tickBientityAction);

        if (tag != null) {
            NbtCompound merged = projectile.writeNbt(new NbtCompound());
            merged.copyFrom(tag);
            projectile.readNbt(merged);
        }

        entity.getWorld().spawnEntity(projectile);

        if (bientityActionAfterFiring != null) {
            bientityActionAfterFiring.accept(new Pair<>(entity, projectile));
        }
    }

    @Override
    public NbtElement toTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("LastUseTime", lastUseTime);
        nbt.putInt("ShotProjectiles", shotProjectiles);
        nbt.putBoolean("FinishedStartDelay", finishedStartDelay);
        nbt.putBoolean("IsFiringProjectiles", isFiringProjectiles);
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtLong) {
            lastUseTime = ((NbtLong) tag).longValue();
        } else if (tag instanceof NbtCompound nbt) {
            lastUseTime = nbt.getLong("LastUseTime");
            shotProjectiles = nbt.getInt("ShotProjectiles");
            finishedStartDelay = nbt.getBoolean("FinishedStartDelay");
            isFiringProjectiles = nbt.getBoolean("IsFiringProjectiles");
        }
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public static PowerFactory<CustomProjectilePower> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("custom_projectile"),
                new SerializableData()
                        .add("cooldown", SerializableDataTypes.INT, 1)
                        .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                        .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                        .add("texture_location", SerializableDataTypes.IDENTIFIER, null)
                        .add("count", SerializableDataTypes.INT, 1)
                        .add("interval", SerializableDataTypes.INT, 0)
                        .add("start_delay", SerializableDataTypes.INT, 0)
                        .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                        .add("divergence", SerializableDataTypes.FLOAT, 1F)
                        .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                        .add("tag", SerializableDataTypes.NBT, null)
                        .add("allow_conditional_cancelling", SerializableDataTypes.BOOLEAN, false)
                        .add("block_action_cancels_miss_action", SerializableDataTypes.BOOLEAN, false)
                        .add("entity_action_before_firing", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("bientity_action_after_firing", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("block_action_on_hit", ApoliDataTypes.BLOCK_ACTION, null)
                        .add("bientity_action_on_miss", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("bientity_action_on_hit", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("owner_target_bientity_action_on_hit", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("tick_bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("owner_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, livingEntity) -> {
                    CustomProjectilePower power = new CustomProjectilePower(
                            type, livingEntity,
                            data.getInt("cooldown"),
                            data.get("hud_render"),
                            data.getId("texture_location"),
                            data.getInt("count"),
                            data.getInt("interval"),
                            data.getInt("start_delay"),
                            data.getFloat("speed"),
                            data.getFloat("divergence"),
                            data.get("sound"),
                            data.get("tag"),
                            data.getBoolean("allow_conditional_cancelling"),
                            data.getBoolean("block_action_cancels_miss_action"),
                            data.get("entity_action_before_firing"),
                            data.get("bientity_action_after_firing"),
                            data.get("block_action_on_hit"),
                            data.get("bientity_action_on_miss"),
                            data.get("bientity_action_on_hit"),
                            data.get("owner_target_bientity_action_on_hit"),
                            data.get("tick_bientity_action"),
                            data.get("block_condition"),
                            data.get("bientity_condition"),
                            data.get("owner_bientity_condition")
                    );
                    power.setKey(data.get("key"));
                    return power;
                }
        ).allowCondition();
    }
}
