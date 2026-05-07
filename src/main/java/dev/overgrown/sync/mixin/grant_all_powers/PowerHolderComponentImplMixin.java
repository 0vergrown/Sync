package dev.overgrown.sync.mixin.grant_all_powers;

import dev.overgrown.sync.action.type.entity.grant_all_powers.SourcePowerRegistry;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.power.Power;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts {@link PowerHolderComponentImpl#addPower(Power, Identifier)} on RETURN
 * to record every successful grant in {@link SourcePowerRegistry}.
 */
@Mixin(value = PowerHolderComponentImpl.class, remap = false)
public class PowerHolderComponentImplMixin {

    @Inject(
        method = "addPower(Lio/github/apace100/apoli/power/Power;Lnet/minecraft/util/Identifier;)Z",
        at = @At("RETURN"),
        remap = false,
        cancellable = false
    )
    private void sync$onAddPower(Power power, Identifier source, CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        SourcePowerRegistry.track(source, power.getId());
    }
}
