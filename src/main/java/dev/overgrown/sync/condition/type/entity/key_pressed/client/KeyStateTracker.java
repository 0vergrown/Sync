package dev.overgrown.sync.condition.type.entity.key_pressed.client;

import dev.overgrown.sync.condition.type.entity.key_pressed.network.c2s.UpdateKeyStatesC2SPacket;
import io.github.apace100.apoli.mixin.KeyBindingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class KeyStateTracker {

    private static Set<String> lastSentPressed = new HashSet<>();

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(KeyStateTracker::onClientTick);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> lastSentPressed = new HashSet<>());
    }

    private static void onClientTick(MinecraftClient client) {

        if (client.player == null || client.world == null) {
            return;
        }

        Set<String> currentlyPressed = new HashSet<>();
        for (Map.Entry<String, KeyBinding> entry : KeyBindingAccessor.getKeysById().entrySet()) {
            if (entry.getValue().isPressed()) {
                currentlyPressed.add(entry.getKey());
            }
        }

        if (!currentlyPressed.equals(lastSentPressed)) {
            ClientPlayNetworking.send(new UpdateKeyStatesC2SPacket(currentlyPressed));
            lastSentPressed = currentlyPressed;
        }
    }
}
