package dev.overgrown.sync.condition.type.entity.player_model_type.util;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerModelTypeManager {

    private static final Map<UUID, String> MODEL_TYPE_CACHE = new ConcurrentHashMap<>();

    public static void setModelType(ServerPlayerEntity player, String modelType) {
        MODEL_TYPE_CACHE.put(player.getUuid(), modelType);
    }

    public static String getModelType(ServerPlayerEntity player) {
        return MODEL_TYPE_CACHE.getOrDefault(player.getUuid(), "wide");
    }

    public static void removePlayer(UUID uuid) {
        MODEL_TYPE_CACHE.remove(uuid);
    }
}
