package dev.overgrown.sync.factory.action.bientity.transfer;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
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
                                     Identifier transferSource, List<PowerType<?>> powers) {
        if (powers.isEmpty()) return;
        if (originalSource.equals(transferSource)) return;

        LinkedHashMap<Identifier, List<PowerType<?>>> packages =
                STOLEN_PACKAGES.computeIfAbsent(actorUUID, k -> new LinkedHashMap<>());

        synchronized (packages) {
            packages.merge(originalSource, new ArrayList<>(powers), (existing, incoming) -> {
                Set<PowerType<?>> seen = new LinkedHashSet<>(existing);
                seen.addAll(incoming);
                return new ArrayList<>(seen);
            });
        }

        Sync.LOGGER.debug("[Sync/SlotManager] Registered {} power(s) under '{}' for {}.",
                powers.size(), originalSource, actorUUID);
    }

    public static void deregisterSource(UUID actorUUID, Identifier originalSource) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages = STOLEN_PACKAGES.get(actorUUID);
        if (packages == null) return;

        synchronized (packages) {
            packages.remove(originalSource);
            if (packages.isEmpty()) {
                STOLEN_PACKAGES.remove(actorUUID);
            }
        }

        LinkedHashMap<Identifier, List<PowerType<?>>> remaining = STOLEN_PACKAGES.get(actorUUID);
        if (remaining == null) {
            SELECTED_INDEX.remove(actorUUID);
        } else {
            int size;
            synchronized (remaining) { size = remaining.size(); }
            if (size == 0) {
                SELECTED_INDEX.remove(actorUUID);
            } else {
                int current = SELECTED_INDEX.getOrDefault(actorUUID, 0);
                SELECTED_INDEX.put(actorUUID, clamp(current, size));
            }
        }

        Sync.LOGGER.debug("[Sync/SlotManager] Deregistered '{}' for {}.", originalSource, actorUUID);
    }

    public static Identifier getSelectedSource(Entity entity, Identifier transferSource) {
        pruneDeadPackages(entity, transferSource);
        List<Identifier> sources = getStolenSources(entity);
        if (sources.isEmpty()) return null;
        int index = clamp(SELECTED_INDEX.getOrDefault(entity.getUuid(), 0), sources.size());
        return sources.get(index);
    }

    public static Identifier getSelectedSource(Entity entity) {
        return getSelectedSource(entity, DEFAULT_SOURCE);
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

    public static void pruneDeadPackages(Entity entity, Identifier transferSource) {
        LinkedHashMap<Identifier, List<PowerType<?>>> packages =
                STOLEN_PACKAGES.get(entity.getUuid());
        if (packages == null) return;

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) return;

        boolean changed = false;
        synchronized (packages) {
            Iterator<Map.Entry<Identifier, List<PowerType<?>>>> iter =
                    packages.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Identifier, List<PowerType<?>>> entry = iter.next();
                boolean anyLive = false;
                for (PowerType<?> pt : entry.getValue()) {
                    if (component.hasPower(pt, transferSource)) {
                        anyLive = true;
                        break;
                    }
                }
                if (!anyLive) {
                    Sync.LOGGER.debug("[Sync/SlotManager] Pruned dead package '{}' for {}.",
                            entry.getKey(), entity.getUuid());
                    iter.remove();
                    changed = true;
                }
            }
            if (packages.isEmpty()) {
                STOLEN_PACKAGES.remove(entity.getUuid());
            }
        }
        if (changed) {
            LinkedHashMap<Identifier, List<PowerType<?>>> remaining =
                    STOLEN_PACKAGES.get(entity.getUuid());
            if (remaining == null) {
                SELECTED_INDEX.remove(entity.getUuid());
            } else {
                int size;
                synchronized (remaining) { size = remaining.size(); }
                if (size == 0) {
                    SELECTED_INDEX.remove(entity.getUuid());
                } else {
                    int current = SELECTED_INDEX.getOrDefault(entity.getUuid(), 0);
                    SELECTED_INDEX.put(entity.getUuid(), clamp(current, size));
                }
            }
        }
    }

    public static void cycle(Entity entity, Identifier transferSource, int delta) {
        pruneDeadPackages(entity, transferSource);
        List<Identifier> sources = getStolenSources(entity);
        if (sources.isEmpty()) {
            SELECTED_INDEX.remove(entity.getUuid());
            if (entity instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("No stolen powers to cycle."), true);
            }
            return;
        }

        int current = clamp(SELECTED_INDEX.getOrDefault(entity.getUuid(), 0), sources.size());
        int next    = Math.floorMod(current + delta, sources.size());
        SELECTED_INDEX.put(entity.getUuid(), next);

        Identifier selected = sources.get(next);
        if (entity instanceof ServerPlayerEntity player) {
            player.sendMessage(
                    Text.literal("[")
                            .append(Text.literal((next + 1) + "/" + sources.size()))
                            .append(Text.literal("] "))
                            .append(Text.literal(selected.toString())),
                    true);
        }

        Sync.LOGGER.debug("[Sync/SlotManager] {} → package {}/{} ('{}').",
                entity.getEntityName(), next + 1, sources.size(), selected);
    }

    public static void cycle(Entity entity, int delta) {
        cycle(entity, DEFAULT_SOURCE, delta);
    }

    public static PowerType<?> getSelectedPowerInPackage(Entity entity) {
        List<PowerType<?>> powers = getSelectedPackagePowers(entity);
        return powers.isEmpty() ? null : powers.get(0);
    }

    public static boolean isPowerInSelectedPackage(Entity entity, PowerType<?> powerType,
                                                   Identifier transferSource) {
        boolean heldAsStolen = PowerHolderComponent.KEY.maybeGet(entity)
                .map(c -> c.hasPower(powerType, transferSource))
                .orElse(false);
        if (!heldAsStolen) return true;
        return getSelectedPackagePowers(entity).contains(powerType);
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

    private static int clamp(int index, int size) {
        if (size <= 0) return 0;
        return Math.max(0, Math.min(index, size - 1));
    }
}