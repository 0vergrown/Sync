package dev.overgrown.sync;

import dev.overgrown.sync.entities.registry.SyncEntityModelLayerRegistry;
import dev.overgrown.sync.entities.registry.SyncEntiyRendererRegistry;
import dev.overgrown.sync.factory.action.entity.radial_menu.client.RadialMenuClient;
import dev.overgrown.sync.networking.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

public class SyncClient implements ClientModInitializer {
    private static final Map<String, Boolean> LAST_KEY_STATES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        RadialMenuClient.register();

        // Register model layers FIRST
        SyncEntityModelLayerRegistry.register();

        // Then register entity renderers
        SyncEntiyRendererRegistry.register();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            for (KeyBinding keyBinding : client.options.allKeys) {
                String key = keyBinding.getTranslationKey();
                boolean pressed = keyBinding.isPressed();
                Boolean lastState = LAST_KEY_STATES.get(key);

                if (lastState == null || lastState != pressed) {
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeString(key);
                    buf.writeBoolean(pressed);
                    ClientPlayNetworking.send(ModPackets.KEY_PRESS_UPDATE, buf);
                    LAST_KEY_STATES.put(key, pressed);
                }
            }
        });
    }
}