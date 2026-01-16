package dev.overgrown.sync.mixin;

import dev.overgrown.sync.factory.power.type.MobsIgnorePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class MobsIgnorePowerHostileEntityMixin {
    @Inject(method = "isAngryAt", at = @At("HEAD"), cancellable = true)
    private void sync$isAngryAt(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        HostileEntity hostile = (HostileEntity)(Object)this;
        PowerHolderComponent.getPowers(player, MobsIgnorePower.class).forEach(power -> {
            if (power.shouldIgnore(hostile)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        });
    }
}