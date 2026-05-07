package dev.overgrown.sync.data.disguise.client;

import dev.overgrown.sync.data.disguise.payload.s2c.DisguiseUpdatePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class DisguiseClientInit {

    private DisguiseClientInit() {}

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DisguiseUpdatePayload.ID,
            (payload, context) -> context.client().execute(() -> {
                if (payload.data().isPresent()) {
                    ClientDisguiseManager.setDisguise(payload.entityNetId(), payload.data().get());
                } else {
                    ClientDisguiseManager.removeDisguise(payload.entityNetId());
                }
            }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(ClientDisguiseManager::clear)
        );

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && entity == client.player) {
                ClientDisguiseManager.clear();
            }
        });
    }
}
