package dev.overgrown.sync.network;

import dev.overgrown.sync.condition.type.entity.key_pressed.network.c2s.UpdateKeyStatesC2SPacket;
import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyStateManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SyncNetworking {

    public static void register() {

        PayloadTypeRegistry.playC2S().register(UpdateKeyStatesC2SPacket.PACKET_ID, UpdateKeyStatesC2SPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateKeyStatesC2SPacket.PACKET_ID, (payload, context) ->
            PlayerKeyStateManager.getOrCreate(context.player()).update(payload.pressedKeys())
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PlayerKeyStateManager.remove(handler.player)
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> PlayerKeyStateManager.tickAll());
    }
}
