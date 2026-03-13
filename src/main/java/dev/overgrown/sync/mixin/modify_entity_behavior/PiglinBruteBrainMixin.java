package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinBruteBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PiglinBruteBrain.class)
public class PiglinBruteBrainMixin {

    @Inject(
            method = "getTargetIfInRange",
            at = @At(
                    "RETURN"
            ),
            cancellable = true
    )
    private static void sync$preventTargeting(AbstractPiglinEntity piglin, MemoryModuleType<? extends LivingEntity> targetMemoryModule, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        if (cir.getReturnValue().isPresent()) {
            //noinspection DataFlowIssue
            piglin.getBrain().getOptionalMemory(targetMemoryModule).filter(livingEntity -> {
                BehaviorHelper behaviorHelper = new BehaviorHelper(livingEntity, piglin);

                if (behaviorHelper.checkEntity()) {
                    if (behaviorHelper.neutralOrPassive()) {
                        cir.setReturnValue(Optional.empty());
                    }
                }

                return false;
            });
        }
    }

    @Inject(
            method = "tryRevenge",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$lobotomizePiglinBrute(PiglinBruteEntity piglinBrute, LivingEntity target, CallbackInfo ci) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(target, piglinBrute);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE)) {
                ci.cancel();
            }
        }
    }
}