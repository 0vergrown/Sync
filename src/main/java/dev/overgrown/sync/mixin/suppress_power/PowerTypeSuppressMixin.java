package dev.overgrown.sync.mixin.suppress_power;

import dev.overgrown.sync.data.suppress_power.SuppressedPowerManager;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts {@link PowerType#isActive()} so any power marked suppressed in
 * {@link SuppressedPowerManager} reports inactive without being removed from
 * the holder's component.
 *
 * <p>1.21 rewrite: the underlying isActive contract moved from
 * {@code Power.isActive()} (1.20.x) to {@code PowerType.isActive()}.</p>
 */
@Mixin(value = PowerType.class, remap = false)
public abstract class PowerTypeSuppressMixin {

    @org.spongepowered.asm.mixin.Shadow
    public abstract @NotNull PowerConfiguration<?> getConfig();

    @org.spongepowered.asm.mixin.Shadow
    public abstract LivingEntity getHolder();

    @org.spongepowered.asm.mixin.Shadow
    public abstract boolean isInitialized();

    @Inject(method = "isActive", at = @At("RETURN"), cancellable = true, remap = false)
    private void sync$checkSuppression(CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (!isInitialized()) return;
        LivingEntity holder = getHolder();
        if (holder == null) return;

        if (SuppressedPowerManager.isSuppressed(holder.getUuid(), getConfig().id())) {
            cir.setReturnValue(false);
        }
    }
}
