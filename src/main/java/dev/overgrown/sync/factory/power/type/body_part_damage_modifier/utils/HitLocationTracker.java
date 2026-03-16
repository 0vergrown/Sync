package dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HitLocationTracker {
    private static final Map<UUID, Vec3d> LAST_HIT = new ConcurrentHashMap<>();

    private HitLocationTracker() {}

    public static void record(LivingEntity entity, double xNorm, double yNorm, double zNorm) {
        if (entity == null) return;
        LAST_HIT.put(entity.getUuid(), new Vec3d(xNorm, yNorm, zNorm));
    }

    public static Vec3d getAndClear(LivingEntity entity) {
        if (entity == null) return null;
        return LAST_HIT.remove(entity.getUuid());
    }

    public static void remove(UUID uuid) {
        LAST_HIT.remove(uuid);
    }
}