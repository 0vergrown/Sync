package dev.overgrown.sync.mixin.grant_all_powers;

import dev.overgrown.sync.factory.action.entity.grant_all_powers.SourcePowerRegistry;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the {@code RETURN} point of
 * {@link PowerHolderComponentImpl#addPower(PowerType, Identifier)} to record
 * every successful grant in {@link SourcePowerRegistry}.
 *
 * <p>Only successful grants (return value {@code true}) are tracked.
 * {@link PowerTypeReference}s are resolved to their underlying type before
 * recording so the stored identifier always points to a concrete power.
 *
 * <h3>Memory considerations</h3>
 * The registry accumulates every unique (source, powerId) pair seen across
 * the session.  It is cleared via {@link SourcePowerRegistry#clear()} on
 * every data-pack reload ({@link io.github.apace100.apoli.integration.PowerClearCallback}),
 * keeping the map bounded to the set of powers actually loaded by the current
 * data packs.
 */
@Mixin(value = PowerHolderComponentImpl.class, remap = false)
public class PowerHolderComponentImplMixin {

    @Inject(
            method = "addPower",
            at = @At(
                    "RETURN"
            ),
            remap = false,
            cancellable = false
    )
    private void sync$onAddPower(PowerType<?> powerType,
                                 Identifier source,
                                 CallbackInfoReturnable<Boolean> cir) {
        // Only track successful additions
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        // Resolve PowerTypeReference -> concrete PowerType
        PowerType<?> resolved = powerType;
        if (powerType instanceof PowerTypeReference<?> ref) {
            PowerType<?> inner = ref.getReferencedPowerType();
            if (inner != null) resolved = inner;
        }

        SourcePowerRegistry.track(source, resolved.getIdentifier());
    }
}