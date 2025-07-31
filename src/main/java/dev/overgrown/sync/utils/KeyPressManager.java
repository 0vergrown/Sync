package dev.overgrown.sync.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeyPressManager {
    private static final Map<UUID, Map<String, KeyState>> PLAYER_KEY_STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Boolean>> PREVIOUS_KEY_STATES = new ConcurrentHashMap<>();

    public static void updateKeyState(UUID playerUuid, String key, boolean pressed) {
        PLAYER_KEY_STATES.computeIfAbsent(playerUuid, k -> new HashMap<>())
                .compute(key, (k, state) -> {
                    if (state == null) {
                        return new KeyState(pressed, Util.getMeasuringTimeMs());
                    }
                    state.current = pressed;
                    state.lastUpdateTime = Util.getMeasuringTimeMs();
                    return state;
                });
    }

    public static boolean getKeyState(UUID playerUuid, String key, boolean continuous) {
        Map<String, KeyState> playerKeys = PLAYER_KEY_STATES.get(playerUuid);
        if (playerKeys == null) return false;

        KeyState state = playerKeys.get(key);
        if (state == null) return false;

        if (continuous) {
            return state.current;
        } else {
            Map<String, Boolean> previousStates = PREVIOUS_KEY_STATES.computeIfAbsent(playerUuid, k -> new HashMap<>());
            boolean wasPressed = previousStates.getOrDefault(key, false);
            boolean isPressed = state.current;
            previousStates.put(key, isPressed);
            return isPressed && !wasPressed;
        }
    }

    public static void removePlayer(UUID playerUuid) {
        PLAYER_KEY_STATES.remove(playerUuid);
        PREVIOUS_KEY_STATES.remove(playerUuid);
    }

    public static void serverTick(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID uuid = player.getUuid();
            Map<String, Boolean> previousStates = PREVIOUS_KEY_STATES.computeIfAbsent(uuid, k -> new HashMap<>());
            Map<String, KeyState> currentStates = PLAYER_KEY_STATES.get(uuid);

            if (currentStates != null) {
                currentStates.forEach((key, state) -> {
                    previousStates.put(key, state.current);
                });
            }
        });
    }

    private static class KeyState {
        boolean current;
        long lastUpdateTime;

        KeyState(boolean current, long lastUpdateTime) {
            this.current = current;
            this.lastUpdateTime = lastUpdateTime;
        }
    }
}