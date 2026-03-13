package dev.overgrown.sync.mixin.modify_entity_behavior;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.AxolotlAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AxolotlAttackablesSensor.class)
public abstract class AxolotlAttackablesSensorMixin {

    @ModifyExpressionValue(
            method = "matches",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/sensor/AxolotlAttackablesSensor;isAlwaysHostileTo(Lnet/minecraft/entity/LivingEntity;)Z"
            )
    )
    private boolean sync$markPlayerAsHostile(boolean original, LivingEntity entity, LivingEntity target) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(target, entity);

        return original || behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.HOSTILE);
    }
}