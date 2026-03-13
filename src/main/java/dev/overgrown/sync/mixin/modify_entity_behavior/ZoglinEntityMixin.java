package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Hoglin;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ZoglinEntity.class)
public abstract class ZoglinEntityMixin extends HostileEntity implements Monster, Hoglin {

    protected ZoglinEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "getHoglinTarget",
            at = @At(
                    "RETURN"
            ),
            cancellable = true
    )
    private void sync$preventTargeting(CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> entity = cir.getReturnValue();

        if (entity.isPresent()) {
            LivingEntity target = entity.get();

            if (target instanceof PlayerEntity player) {
                BehaviorHelper behaviorHelper = new BehaviorHelper(player, this);

                if (behaviorHelper.checkEntity()) {
                    if (behaviorHelper.neutralOrPassive()) {
                        cir.setReturnValue(Optional.empty());
                    }
                }
            }
        }
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/ZoglinEntity;setAttackTarget(Lnet/minecraft/entity/LivingEntity;)V"
            ),
            cancellable = true
    )
    private void sync$lobotomizeZoglin(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = source.getAttacker();
        if (!(entity instanceof PlayerEntity player)) return;

        BehaviorHelper behaviorHelper = new BehaviorHelper(player, this);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE)) {
                cir.setReturnValue(true);
            }
        }
    }
}