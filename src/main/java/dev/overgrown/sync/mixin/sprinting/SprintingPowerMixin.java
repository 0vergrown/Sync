package dev.overgrown.sync.mixin.sprinting;

import dev.overgrown.sync.power.type.sprinting.SprintingPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
public class SprintingPowerMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void sync$forceSprintingFromPower(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        List<SprintingPowerType> powers = PowerHolderComponent.getPowerTypes(entity, SprintingPowerType.class);
        for (SprintingPowerType power : powers) {
            if (power.isActive() && power.shouldSprint()) {
                entity.setSprinting(true);
                break;
            }
        }
    }
}
