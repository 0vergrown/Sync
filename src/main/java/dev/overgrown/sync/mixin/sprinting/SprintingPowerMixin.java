package dev.overgrown.sync.mixin.sprinting;

import dev.overgrown.sync.factory.power.type.sprinting.SprintingPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class SprintingPowerMixin {

    @Inject(
            method = "tickMovement",
            at = @At(
                    "HEAD"
            )
    )
    private void sync$forceSprintingFromPower(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Check if entity has any SprintingPower
        PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
        for (SprintingPower power : component.getPowers(SprintingPower.class)) {
            if (power.isActive() && power.shouldSprint()) {
                entity.setSprinting(true);
                break; // Only need one power to force sprinting
            }
        }
    }
}