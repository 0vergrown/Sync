package dev.overgrown.sync.mixin.integration.connector;

import dev.overgrown.sync.networking.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(MinecraftClient.class)
public abstract class ClientTickEventMixin {

    @Shadow
    @Final
    public net.minecraft.client.option.GameOptions options;

    @Shadow
    public net.minecraft.client.network.ClientPlayerEntity player;

    @Unique
    private static final Map<String, Boolean> sync$lastKeyStates = new HashMap<>();

    @Unique
    private static String sync$lastModelType = null;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void sync$onClientTick(CallbackInfo ci) {
        if (this.player == null) return;

        // Player Model Type Detection
        String currentModelType = this.player.getModel();

        // Convert "default" to "wide" for consistency
        if (currentModelType.equals("default")) {
            currentModelType = "wide";
        }

        if (sync$lastModelType == null || !sync$lastModelType.equals(currentModelType)) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeString(currentModelType);
            ClientPlayNetworking.send(ModPackets.PLAYER_MODEL_TYPE_UPDATE, buf);
            sync$lastModelType = currentModelType;
        }

        // Key Pressed Condition
        for (KeyBinding keyBinding : this.options.allKeys) {
            String key = keyBinding.getTranslationKey();
            boolean pressed = keyBinding.isPressed();
            Boolean lastState = sync$lastKeyStates.get(key);

            if (lastState == null || lastState != pressed) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeString(key);
                buf.writeBoolean(pressed);
                ClientPlayNetworking.send(ModPackets.KEY_PRESS_UPDATE, buf);
                sync$lastKeyStates.put(key, pressed);
            }
        }
    }
}