package dev.overgrown.sync.data.rope.common;

import dev.overgrown.sync.data.rope.payload.c2s.RopeChangeLengthPayload;
import dev.overgrown.sync.data.rope.payload.c2s.RopeSwingPayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeCreatePayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeDeletePayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeVerletLengthPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class RopeInit {

    private RopeInit() {}

    public static void init() {
        // Payload registration
        PayloadTypeRegistry.playC2S().register(RopeChangeLengthPayload.ID, RopeChangeLengthPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RopeSwingPayload.ID, RopeSwingPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RopeCreatePayload.ID, RopeCreatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RopeDeletePayload.ID, RopeDeletePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RopeVerletLengthPayload.ID, RopeVerletLengthPayload.CODEC);

        // Server tick: physics for all players with ropes
        ServerTickEvents.END_SERVER_TICK.register(RopeManager::tick);

        // C2S: player requesting rope length change
        ServerPlayNetworking.registerGlobalReceiver(RopeChangeLengthPayload.ID,
            (payload, context) -> context.server().execute(() -> {
                if (context.player().getUuid().equals(payload.owner())) {
                    RopeManager.handleChangeLength(context.player(), payload.delta());
                }
            }));

        // C2S: player swing input
        ServerPlayNetworking.registerGlobalReceiver(RopeSwingPayload.ID,
            (payload, context) -> context.server().execute(() ->
                RopeManager.handleSwing(context.player(), payload.inputDir())
            ));
    }
}
