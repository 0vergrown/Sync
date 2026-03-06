package dev.overgrown.sync.mixin.suppress_power;

import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the {@code RETURN} of {@link Power#isActive()} so that any power
 * marked as suppressed in {@link SuppressedPowerManager} reports itself as
 * inactive without actually being removed from the component.
 *
 * <p>The injection only fires when the base method would return {@code true},
 * preserving the existing condition logic (e.g. a power that is already
 * condition-gated to {@code false} stays {@code false}).
 */
@Mixin(value = Power.class, remap = false)
public abstract class PowerSuppressMixin {

    @Shadow protected LivingEntity entity;
    @Shadow protected PowerType<?> type;

    @Inject(
            method = "isActive",
            at = @At(
                    "RETURN"
            ),
            cancellable = true,
            remap = false
    )
    private void sync$checkSuppression(CallbackInfoReturnable<Boolean> cir) {
        // Only override when the power would otherwise be active.
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (entity == null || type == null) return;

        if (SuppressedPowerManager.isSuppressed(entity.getUuid(), type.getIdentifier())) {
            cir.setReturnValue(false);
        }
    }
}