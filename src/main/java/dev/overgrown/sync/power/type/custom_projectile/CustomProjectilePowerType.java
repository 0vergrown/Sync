package dev.overgrown.sync.power.type.custom_projectile;

import dev.overgrown.sync.power.type.custom_projectile.entities.CustomProjectileEntity;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.ActiveCooldownPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomProjectilePowerType extends ActiveCooldownPowerType {

    public static final TypedDataObjectFactory<CustomProjectilePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("cooldown", SerializableDataTypes.INT, 1)
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("key", ApoliDataTypes.KEY, KeyBindingReference.NONE)
            .add("texture_location", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("count", SerializableDataTypes.INT, 1)
            .add("interval", SerializableDataTypes.INT, 0)
            .add("start_delay", SerializableDataTypes.INT, 0)
            .add("speed", SerializableDataTypes.FLOAT, 1.5F)
            .add("divergence", SerializableDataTypes.FLOAT, 1F)
            .add("sound", SerializableDataTypes.SOUND_EVENT.optional(), Optional.empty())
            .add("tag", SerializableDataTypes.NBT_COMPOUND.optional(), Optional.empty())
            .add("allow_conditional_cancelling", SerializableDataTypes.BOOLEAN, false)
            .add("block_action_cancels_miss_action", SerializableDataTypes.BOOLEAN, false)
            .add("entity_action_before_firing", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action_after_firing", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action_on_hit", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action_on_miss", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action_on_hit", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("owner_target_bientity_action_on_hit", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("tick_bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("owner_bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new CustomProjectilePowerType(
            data.get("hud_render"),
            data.get("cooldown"),
            data.get("key"),
            data.get("texture_location"),
            data.get("count"),
            data.get("interval"),
            data.get("start_delay"),
            data.get("speed"),
            data.get("divergence"),
            data.get("sound"),
            data.get("tag"),
            data.get("allow_conditional_cancelling"),
            data.get("block_action_cancels_miss_action"),
            data.get("entity_action_before_firing"),
            data.get("bientity_action_after_firing"),
            data.get("block_action_on_hit"),
            data.get("bientity_action_on_miss"),
            data.get("bientity_action_on_hit"),
            data.get("owner_target_bientity_action_on_hit"),
            data.get("tick_bientity_action"),
            data.get("block_condition"),
            data.get("bientity_condition"),
            data.get("owner_bientity_condition"),
            condition
        ),
        (powerType, sd) -> sd.instance()
            .set("cooldown", powerType.getCooldown())
            .set("hud_render", powerType.getRenderSettings())
            .set("key", powerType.getKey())
            .set("texture_location", powerType.textureLocation)
            .set("count", powerType.count)
            .set("interval", powerType.interval)
            .set("start_delay", powerType.startDelay)
            .set("speed", powerType.speed)
            .set("divergence", powerType.divergence)
            .set("sound", powerType.sound)
            .set("tag", powerType.tag)
            .set("allow_conditional_cancelling", powerType.allowConditionalCancelling)
            .set("block_action_cancels_miss_action", powerType.blockActionCancelsMissAction)
            .set("entity_action_before_firing", powerType.entityActionBeforeFiring)
            .set("bientity_action_after_firing", powerType.bientityActionAfterFiring)
            .set("block_action_on_hit", powerType.blockActionOnHit)
            .set("bientity_action_on_miss", powerType.bientityActionOnMiss)
            .set("bientity_action_on_hit", powerType.bientityActionOnHit)
            .set("owner_target_bientity_action_on_hit", powerType.ownerTargetBientityActionOnHit)
            .set("tick_bientity_action", powerType.tickBientityAction)
            .set("block_condition", powerType.blockCondition)
            .set("bientity_condition", powerType.bientityCondition)
            .set("owner_bientity_condition", powerType.ownerBientityCondition)
    );

    private int shotProjectiles;
    private boolean finishedStartDelay;
    private boolean isFiringProjectiles;

    private final Optional<Identifier> textureLocation;
    private final int count;
    private final int interval;
    private final int startDelay;
    private final float speed;
    private final float divergence;
    private final Optional<SoundEvent> sound;
    private final Optional<NbtCompound> tag;
    private final boolean allowConditionalCancelling;
    private final boolean blockActionCancelsMissAction;

    private final Optional<EntityAction> entityActionBeforeFiring;
    private final Optional<BiEntityAction> bientityActionAfterFiring;
    private final Optional<BlockAction> blockActionOnHit;
    private final Optional<BiEntityAction> bientityActionOnMiss;
    private final Optional<BiEntityAction> bientityActionOnHit;
    private final Optional<BiEntityAction> ownerTargetBientityActionOnHit;
    private final Optional<BiEntityAction> tickBientityAction;

    private final Optional<BlockCondition> blockCondition;
    private final Optional<BiEntityCondition> bientityCondition;
    private final Optional<BiEntityCondition> ownerBientityCondition;

    public CustomProjectilePowerType(HudRender hudRender, int cooldownDuration, KeyBindingReference key,
                                     Optional<Identifier> textureLocation, int count, int interval, int startDelay,
                                     float speed, float divergence, Optional<SoundEvent> sound,
                                     Optional<NbtCompound> tag, boolean allowConditionalCancelling,
                                     boolean blockActionCancelsMissAction,
                                     Optional<EntityAction> entityActionBeforeFiring,
                                     Optional<BiEntityAction> bientityActionAfterFiring,
                                     Optional<BlockAction> blockActionOnHit,
                                     Optional<BiEntityAction> bientityActionOnMiss,
                                     Optional<BiEntityAction> bientityActionOnHit,
                                     Optional<BiEntityAction> ownerTargetBientityActionOnHit,
                                     Optional<BiEntityAction> tickBientityAction,
                                     Optional<BlockCondition> blockCondition,
                                     Optional<BiEntityCondition> bientityCondition,
                                     Optional<BiEntityCondition> ownerBientityCondition,
                                     Optional<EntityCondition> condition) {
        super(hudRender, cooldownDuration, key, condition);
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
        setTicking();
    }

    @Override
    public void onUse() {
        if (canUse()) {
            isFiringProjectiles = true;
            use();
        }
    }

    @Override
    public void serverTick() {
        if (!isFiringProjectiles) return;

        if (!finishedStartDelay && startDelay == 0) {
            finishedStartDelay = true;
        }

        long timeSinceUse = getHolder().getEntityWorld().getTime() - lastUseTime;

        if (!finishedStartDelay && startDelay > 0 && timeSinceUse % startDelay == 0) {
            finishedStartDelay = true;
            shotProjectiles++;
            if (shotProjectiles <= count) {
                playSound();
                if (!getHolder().getWorld().isClient) {
                    fireProjectile();
                }
            } else {
                reset();
            }
        } else if (interval == 0 && finishedStartDelay) {
            playSound();
            if (!getHolder().getWorld().isClient) {
                while (shotProjectiles < count) {
                    fireProjectile();
                    shotProjectiles++;
                }
            }
            reset();
        } else if (finishedStartDelay && interval > 0 && timeSinceUse % interval == 0) {
            shotProjectiles++;
            if (shotProjectiles <= count) {
                playSound();
                if (!getHolder().getWorld().isClient) {
                    fireProjectile();
                }
            } else {
                reset();
            }
        }
    }

    private void playSound() {
        sound.ifPresent(s -> {
            LivingEntity holder = getHolder();
            holder.getWorld().playSound(null, holder.getX(), holder.getY(), holder.getZ(),
                s, SoundCategory.NEUTRAL, 0.5F,
                0.4F / (holder.getRandom().nextFloat() * 0.4F + 0.8F));
        });
    }

    private void reset() {
        shotProjectiles = 0;
        finishedStartDelay = false;
        isFiringProjectiles = false;
    }

    private void fireProjectile() {
        LivingEntity holder = getHolder();
        entityActionBeforeFiring.ifPresent(a -> a.accept(new EntityActionContext(holder)));

        if (allowConditionalCancelling && !isActive()) {
            isFiringProjectiles = false;
            return;
        }

        Vec3d rotationVec = holder.getRotationVector();
        Vec3d spawnPos = new Vec3d(holder.getX(), holder.getEyeY(), holder.getZ()).add(rotationVec);

        CustomProjectileEntity projectile = new CustomProjectileEntity(
            spawnPos.x, spawnPos.y, spawnPos.z, holder, holder.getWorld());

        projectile.setVelocity(holder, holder.getPitch(), holder.getYaw(), 0.0F, speed, divergence * 0.075F);
        projectile.setEntityId(getConfig().id());
        textureLocation.ifPresent(projectile::setTextureLocation);
        blockActionOnHit.ifPresent(projectile::setBlockAction);
        projectile.setBlockActionCancelsMissAction(blockActionCancelsMissAction);
        bientityActionOnMiss.ifPresent(projectile::setMissBiEntityAction);
        bientityActionOnHit.ifPresent(projectile::setImpactBiEntityAction);
        ownerTargetBientityActionOnHit.ifPresent(projectile::setOwnerImpactBiEntityAction);
        blockCondition.ifPresent(projectile::setBlockCondition);
        bientityCondition.ifPresent(projectile::setBiEntityCondition);
        ownerBientityCondition.ifPresent(projectile::setOwnerBiEntityCondition);
        tickBientityAction.ifPresent(projectile::setTickBiEntityAction);

        if (tag.isPresent()) {
            NbtCompound merged = projectile.writeNbt(new NbtCompound());
            merged.copyFrom(tag.get());
            projectile.readNbt(merged);
        }

        holder.getWorld().spawnEntity(projectile);

        bientityActionAfterFiring.ifPresent(a -> a.accept(new BiEntityActionContext(holder, projectile)));
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
        if (tag instanceof NbtLong nl) {
            lastUseTime = nl.longValue();
        } else if (tag instanceof NbtCompound nbt) {
            lastUseTime = nbt.getLong("LastUseTime");
            shotProjectiles = nbt.getInt("ShotProjectiles");
            finishedStartDelay = nbt.getBoolean("FinishedStartDelay");
            isFiringProjectiles = nbt.getBoolean("IsFiringProjectiles");
        }
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.CUSTOM_PROJECTILE;
    }
}
