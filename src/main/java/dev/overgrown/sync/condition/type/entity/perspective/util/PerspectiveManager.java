package dev.overgrown.sync.condition.type.entity.perspective.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PerspectiveManager {

    private static final Map<UUID, String> PERSPECTIVES = new ConcurrentHashMap<>();

    public static void setPerspective(ServerPlayerEntity player, String perspective) {
        PERSPECTIVES.put(player.getUuid(), perspective);
    }

    public static String getPerspective(PlayerEntity player) {
        return PERSPECTIVES.getOrDefault(player.getUuid(), "first_person");
    }

    public static void removePlayer(UUID uuid) {
        PERSPECTIVES.remove(uuid);
    }
}
