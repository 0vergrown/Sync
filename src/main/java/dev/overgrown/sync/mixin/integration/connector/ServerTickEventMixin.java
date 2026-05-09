package dev.overgrown.sync.mixin.integration.connector;

import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ServerTickEventMixin {

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(
                    "TAIL"
            )
    )
    private void sync$onServerTick(CallbackInfo ci) {
        KeyPressManager.serverTick((MinecraftServer)(Object)this);
    }
}