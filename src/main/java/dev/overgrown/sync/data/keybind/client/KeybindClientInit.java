package dev.overgrown.sync.data.keybind.client;

import dev.overgrown.sync.data.keybind.payload.s2c.KeybindSyncPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class KeybindClientInit {

    private KeybindClientInit() {}

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(KeybindSyncPayload.ID, (payload, context) ->
            context.client().execute(() -> DynamicKeyBindingManager.applyKeybinds(payload.definitions()))
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(DynamicKeyBindingManager::unregisterAll)
        );
    }
}
