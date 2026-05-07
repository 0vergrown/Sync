package dev.overgrown.sync.data.transfer;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks "stolen" power packages per actor: the original source-identifier of
 * each batch of powers transferred from another entity, plus the actor's
 * currently-selected source for "give" semantics.
 *
 * <p>1.21 rewrite: works with {@link Power} objects (the new wrapper) rather
 * than 1.20.x's {@code PowerType<?>}. Sub-power filtering routes through
 * {@link MultiplePower#getSubPowerIds()}.</p>
 */
public final class StolenPowerSlotManager {

    private StolenPowerSlotManager() {}

    public static final Identifier DEFAULT_SOURCE = Sync.identifier("transferred");

    private static final Map<UUID, LinkedHashMap<Identifier, List<Power>>> STOLEN_PACKAGES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SELECTED_INDEX = new ConcurrentHashMap<>();

    public static void registerSteal(UUID actorUuid, Identifier originalSource, List<Power> powers) {
        List<Power> topLevel = filterTopLevel(powers);
        if (topLevel.isEmpty()) return;

        LinkedHashMap<Identifier, List<Power>> packages =
            STOLEN_PACKAGES.computeIfAbsent(actorUuid, k -> new LinkedHashMap<>());

        synchronized (packages) {
            packages.merge(originalSource, new ArrayList<>(topLevel), (existing, incoming) -> {
                Set<Power> seen = new LinkedHashSet<>(existing);
                seen.addAll(incoming);
                return new ArrayList<>(seen);
            });
        }

        SELECTED_INDEX.putIfAbsent(actorUuid, 0);
    }

    public static void deregisterSource(UUID actorUuid, Identifier originalSource) {
        LinkedHashMap<Identifier, List<Power>> packages = STOLEN_PACKAGES.get(actorUuid);
        if (packages == null) return;

        synchronized (packages) {
            packages.remove(originalSource);
            if (packages.isEmpty()) {
                STOLEN_PACKAGES.remove(actorUuid);
                SELECTED_INDEX.remove(actorUuid);
                return;
            }
        }

        clampIndex(actorUuid);
    }

    public static Identifier getSelectedSource(Entity entity) {
        List<Identifier> sources = getStolenSources(entity);
        if (sources.isEmpty()) return null;
        int index = clamp(SELECTED_INDEX.getOrDefault(entity.getUuid(), 0), sources.size());
        return sources.get(index);
    }

    public static List<Power> getSelectedPackagePowers(Entity entity) {
        Identifier source = getSelectedSource(entity);
        if (source == null) return Collections.emptyList();
        return getPowersForSource(entity, source);
    }

    public static List<Power> getPowersForSource(Entity entity, Identifier originalSource) {
        LinkedHashMap<Identifier, List<Power>> packages = STOLEN_PACKAGES.get(entity.getUuid());
        if (packages == null) return Collections.emptyList();
        synchronized (packages) {
            List<Power> powers = packages.get(originalSource);
            return powers != null ? new ArrayList<>(powers) : Collections.emptyList();
        }
    }

    public static List<Identifier> getStolenSources(Entity entity) {
        LinkedHashMap<Identifier, List<Power>> packages = STOLEN_PACKAGES.get(entity.getUuid());
        if (packages == null) return Collections.emptyList();
        synchronized (packages) {
            if (packages.isEmpty()) return Collections.emptyList();
            return new ArrayList<>(packages.keySet());
        }
    }

    public static boolean hasPackages(Entity entity) {
        LinkedHashMap<Identifier, List<Power>> packages = STOLEN_PACKAGES.get(entity.getUuid());
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

    public static List<Power> filterTopLevel(List<Power> all) {
        if (all.isEmpty()) return Collections.emptyList();
        Set<Identifier> subIds = new HashSet<>();
        for (Power p : all) {
            if (p instanceof MultiplePower mp) {
                subIds.addAll(mp.getSubPowerIds());
            }
        }
        if (subIds.isEmpty()) return new ArrayList<>(all);
        List<Power> result = new ArrayList<>();
        for (Power p : all) {
            if (!subIds.contains(p.getId())) result.add(p);
        }
        return result;
    }

    private static void clampIndex(UUID uuid) {
        LinkedHashMap<Identifier, List<Power>> packages = STOLEN_PACKAGES.get(uuid);
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
