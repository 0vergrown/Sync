package dev.overgrown.sync.condition.type.entity.key_pressed.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerKeyStateManager {

    private static final ConcurrentMap<UUID, PlayerKeyState> STATES = new ConcurrentHashMap<>();

    public static PlayerKeyState getOrCreate(ServerPlayerEntity player) {
        return STATES.computeIfAbsent(player.getUuid(), uuid -> new PlayerKeyState());
    }

    public static PlayerKeyState get(Entity entity) {
        return STATES.get(entity.getUuid());
    }

    public static void remove(ServerPlayerEntity player) {
        STATES.remove(player.getUuid());
    }

    public static void tickAll() {
        for (PlayerKeyState state : STATES.values()) {
            state.tick();
        }
    }
}
