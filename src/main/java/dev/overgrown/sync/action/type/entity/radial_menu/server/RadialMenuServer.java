package dev.overgrown.sync.action.type.entity.radial_menu.server;

import dev.overgrown.sync.action.type.entity.radial_menu.payload.c2s.RadialMenuChoicePayload;
import dev.overgrown.sync.action.type.entity.radial_menu.payload.s2c.OpenRadialMenuPayload;
import io.github.apace100.apoli.action.context.EntityActionContext;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class RadialMenuServer {

    private RadialMenuServer() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenRadialMenuPayload.ID, OpenRadialMenuPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RadialMenuChoicePayload.ID, RadialMenuChoicePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RadialMenuChoicePayload.ID, (payload, context) ->
            context.server().execute(() -> {
                context.player().closeHandledScreen();
                payload.action().accept(new EntityActionContext(context.player()));
            })
        );
    }
}
