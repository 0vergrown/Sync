package dev.overgrown.sync.data.keybind;

import dev.overgrown.sync.data.keybind.payload.s2c.KeybindSyncPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public final class KeybindInit {

    private KeybindInit() {}

    public static void init() {
        PayloadTypeRegistry.playS2C().register(KeybindSyncPayload.ID, KeybindSyncPayload.CODEC);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
            .registerReloadListener(new DataDrivenKeybindLoader());

        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) ->
            ServerPlayNetworking.send(handler.player, new KeybindSyncPayload(DataDrivenKeybindLoader.LOADED))
        );
    }
}
