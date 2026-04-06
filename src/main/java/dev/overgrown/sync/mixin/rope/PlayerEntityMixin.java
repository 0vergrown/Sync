package dev.overgrown.sync.mixin.rope;

import dev.overgrown.sync.rope.common.RopeManager;
import dev.overgrown.sync.rope.common.RopeConstants;
import dev.overgrown.sync.rope.common.RopeState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(
            method = "checkFallFlying",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sync$blockElytraIfRopeTimedOut(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!self.getWorld().isClient()) {
            RopeState rope = RopeManager.get(self.getUuid());
            // Only block new elytra activation if already at the flight time limit
            if (rope != null && rope.playerFlightTicks >= RopeConstants.ELYTRA_TIME_LIMIT) {
                cir.setReturnValue(false);
            }
        }
    }
}
