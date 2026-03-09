package dev.overgrown.sync.factory.action.entity.toggle_transfer_mode.utils;

import dev.overgrown.sync.Sync;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TransferModeManager {

    private TransferModeManager() {}

    private static final Map<UUID, Boolean> MODES = new ConcurrentHashMap<>();

    public static boolean isStealing(Entity entity) {
        return MODES.getOrDefault(entity.getUuid(), true);
    }

    public static String getModeString(Entity entity) {
        return isStealing(entity) ? "STEAL" : "GIVE";
    }

    public static void toggle(Entity entity) {
        boolean newStealing = !isStealing(entity);
        MODES.put(entity.getUuid(), newStealing);

        if (entity instanceof ServerPlayerEntity player) {
            Text msg = newStealing
                    ? Text.literal("⚡ Mode: ").formatted(Formatting.GRAY)
                    .append(Text.literal("STEAL").formatted(Formatting.RED, Formatting.BOLD))
                    : Text.literal("⚡ Mode: ").formatted(Formatting.GRAY)
                    .append(Text.literal("GIVE").formatted(Formatting.GREEN, Formatting.BOLD));
            player.sendMessage(msg, true /* action bar */);
        }

        Sync.LOGGER.debug("[Sync/ModeManager] {} switched to {} mode.",
                entity.getEntityName(), newStealing ? "STEAL" : "GIVE");
    }

    public static void setMode(Entity entity, boolean steal) {
        MODES.put(entity.getUuid(), steal);
    }

    public static void remove(UUID uuid) {
        MODES.remove(uuid);
    }
}