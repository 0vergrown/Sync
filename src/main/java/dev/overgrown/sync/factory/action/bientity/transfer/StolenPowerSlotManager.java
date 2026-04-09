package dev.overgrown.sync.factory.action.bientity.transfer;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class StolenPowerSlotManager {

    private StolenPowerSlotManager() {}

    public static final Identifier DEFAULT_SOURCE = Sync.identifier("transferred");

    private static final Map<UUID, LinkedHashMap<Identifier, List<PowerType<?>>>> STOLEN_PACKAGES =
            new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SELECTED_INDEX = new ConcurrentHashMap<>();

    public static void registerSteal(UUID actorUUID, Identifier originalSource,
                                     List<PowerType<?>> powers) {
        List<PowerType<?>> topLevel = filterTopLevel(powers);
        if (topLevel.isEmpty()) return;

        LinkedHashMap<Identifier, List<PowerType<?>>> packages =
                STOLEN_PACKAGES.computeIfAbsent(actorUUID, k -> new LinkedHashMap<>());

        synchronized (packages) {
            packages.merge(originalSource, new ArrayList<>(topLevel), (existing, incoming) -> {
                Set<PowerType<?>> seen = new LinkedHashSet<>(existing);
                seen.addAll(incoming);
                return new ArrayList<>(seen);
            });
        }

        SELECTED_INDEX.putIfAbsent(actorUUID, 0);
    }

    public static void deregisterSource(UUID actorUUID, Identifier originalSource) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages = STOLEN_PACKAGES.get(actorUUID);
        if (packages == null) return;

        synchronized (packages) {
            packages.remove(originalSource);
            if (packages.isEmpty()) {
                STOLEN_PACKAGES.remove(actorUUID);
                SELECTED_INDEX.remove(actorUUID);
                return;
            }
        }

        clampIndex(actorUUID);
    }

    public static Identifier getSelectedSource(Entity entity) {
        List<Identifier> sources = getStolenSources(entity);
        if (sources.isEmpty()) return null;
        int index = clamp(SELECTED_INDEX.getOrDefault(entity.getUuid(), 0), sources.size());
        return sources.get(index);
    }

    public static List<PowerType<?>> getSelectedPackagePowers(Entity entity) {
        Identifier source = getSelectedSource(entity);
        if (source == null) return Collections.emptyList();
        return getPowersForSource(entity, source);
    }

    public static List<PowerType<?>> getPowersForSource(Entity entity, Identifier originalSource) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages =
                STOLEN_PACKAGES.get(entity.getUuid());
        if (packages == null) return Collections.emptyList();
        synchronized (packages) {
            List<PowerType<?>> powers = packages.get(originalSource);
            return powers != null ? new ArrayList<>(powers) : Collections.emptyList();
        }
    }

    public static List<Identifier> getStolenSources(Entity entity) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages =
                STOLEN_PACKAGES.get(entity.getUuid());
        if (packages == null) return Collections.emptyList();
        synchronized (packages) {
            if (packages.isEmpty()) return Collections.emptyList();
            return new ArrayList<>(packages.keySet());
        }
    }

    public static boolean hasPackages(Entity entity) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages =
                STOLEN_PACKAGES.get(entity.getUuid());
        if (packages == null) return false;
        synchronized (packages) {
            return !packages.isEmpty();
        }
    }

    public static void cycle(Entity entity, int delta) {
        List<Identifier> sources = getStolenSources(entity);
        if (sources.isEmpty()) {
            SELECTED_INDEX.remove(entity.getUuid());
            if (entity instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("No stolen powers to cycle."), true);
            }
            return;
        }

        int current = clamp(SELECTED_INDEX.getOrDefault(entity.getUuid(), 0), sources.size());
        int next = Math.floorMod(current + delta, sources.size());
        SELECTED_INDEX.put(entity.getUuid(), next);

        Identifier selected = sources.get(next);
        if (entity instanceof ServerPlayerEntity player) {
            player.sendMessage(
                    Text.literal("[" + (next + 1) + "/" + sources.size() + "] " + selected),
                    true);
        }
    }

    public static boolean isSourceSelected(Entity entity, Identifier originalSource) {
        Identifier sel = getSelectedSource(entity);
        return sel != null && sel.equals(originalSource);
    }

    public static void remove(UUID uuid) {
        STOLEN_PACKAGES.remove(uuid);
        SELECTED_INDEX.remove(uuid);
    }

    public static List<PowerType<?>> filterTopLevel(List<PowerType<?>> all) {
        if (all.isEmpty()) return Collections.emptyList();
        Set<Identifier> subIds = new HashSet<>();
        for (PowerType<?> pt : all) {
            if (pt instanceof MultiplePowerType<?> mpt && mpt.getSubPowers() != null) {
                subIds.addAll(mpt.getSubPowers());
            }
        }
        if (subIds.isEmpty()) return new ArrayList<>(all);
        List<PowerType<?>> result = new ArrayList<>();
        for (PowerType<?> pt : all) {
            if (!subIds.contains(pt.getIdentifier())) result.add(pt);
        }
        return result;
    }

    private static void clampIndex(UUID uuid) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages = STOLEN_PACKAGES.get(uuid);
        if (packages == null) {
            SELECTED_INDEX.remove(uuid);
            return;
        }
        int size;
        synchronized (packages) { size = packages.size(); }
        if (size == 0) {
            SELECTED_INDEX.remove(uuid);
        } else {
            int current = SELECTED_INDEX.getOrDefault(uuid, 0);
            SELECTED_INDEX.put(uuid, clamp(current, size));
        }
    }

    private static int clamp(int index, int size) {
        if (size <= 0) return 0;
        return Math.max(0, Math.min(index, size - 1));
    }
}
