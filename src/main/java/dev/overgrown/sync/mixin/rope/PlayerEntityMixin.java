package dev.overgrown.sync.mixin.rope;

import dev.overgrown.sync.rope.common.RopeManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(
            method = "checkFallFlying",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$blockElytraIfRoped(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        // Only cancel elytra activation on the server side, and only if rope is attached
        if (!self.getWorld().isClient() && RopeManager.has(self.getUuid())) {
            cir.setReturnValue(false);
        }
    }
}