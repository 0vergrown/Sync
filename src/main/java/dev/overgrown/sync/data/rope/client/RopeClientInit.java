package dev.overgrown.sync.data.rope.client;

import dev.overgrown.sync.data.rope.payload.s2c.RopeCreatePayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeDeletePayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeVerletLengthPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class RopeClientInit {

    private RopeClientInit() {}

    public static void init() {
        RopeRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> RopeClientManager.tick());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RopeClientManager.clear());

        ClientPlayNetworking.registerGlobalReceiver(RopeCreatePayload.ID, (payload, context) ->
            context.client().execute(() -> RopeClientManager.create(
                payload.ropeId(),
                payload.owner(),
                payload.anchor(),
                payload.length(),
                payload.maxLength(),
                payload.texture(),
                payload.anchorEntityId(),
                payload.leash()
            ))
        );

        ClientPlayNetworking.registerGlobalReceiver(RopeDeletePayload.ID, (payload, context) ->
            context.client().execute(() -> RopeClientManager.delete(payload.ropeId()))
        );

        ClientPlayNetworking.registerGlobalReceiver(RopeVerletLengthPayload.ID, (payload, context) ->
            context.client().execute(() -> RopeClientManager.setTargetLength(payload.ropeId(), payload.length()))
        );
    }
}
