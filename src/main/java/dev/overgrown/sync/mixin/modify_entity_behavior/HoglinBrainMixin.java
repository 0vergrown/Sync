package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinBrain;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoglinBrain.class)
public class HoglinBrainMixin {

    @Inject(
            method = "getNearestVisibleTargetablePlayer",
            at = @At(
                    "RETURN"
            ),
            cancellable = true
    )
    private static void sync$preventTargeting(HoglinEntity hoglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> entity = cir.getReturnValue();

        if (entity.isPresent()) {
            LivingEntity target = entity.get();

            if (target instanceof PlayerEntity player) {
                BehaviorHelper behaviorHelper = new BehaviorHelper(player, hoglin);

                if (behaviorHelper.checkEntity()) {
                    if (behaviorHelper.neutralOrPassive()) {
                        cir.setReturnValue(Optional.empty());
                    }
                }
            }
        }
    }

    @Inject(
            method = "onAttacked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/HoglinBrain;targetEnemy(Lnet/minecraft/entity/mob/HoglinEntity;Lnet/minecraft/entity/LivingEntity;)V"
            ),
            cancellable = true
    )
    private static void sync$lobotomizeHoglin(HoglinEntity hoglin, LivingEntity attacker, CallbackInfo ci) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(attacker, hoglin);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE)) {
                ci.cancel();
            }
        }
    }
}