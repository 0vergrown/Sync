package dev.overgrown.sync.mixin.body_part_damage_modifier;

import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.HitLocationTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Records where a player's melee attack lands on the target entity
 * by ray-casting from the attacker's eye along their look direction.
 * The hit location is stored on the TARGET so that the target's
 * BodyPartDamageModifierPower can read it when damage is applied.
 */
@Mixin(PlayerEntity.class)
public class MeleeAttackBodyHitMixin {

    @Inject(
            method = "attack",
            at = @At(
                    "HEAD"
            )
    )
    private void sync$recordMeleeBodyHit(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity living)) return;
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().isClient) return;

        Vec3d eye  = self.getEyePos();
        Vec3d look = self.getRotationVec(1.0f).normalize();
        Vec3d end  = eye.add(look.multiply(6.0));

        Box box = living.getBoundingBox();
        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        double height = Math.max(box.maxY - box.minY, 1e-3);
        double halfWidth = Math.max(living.getWidth() * 0.5, 1e-3);

        // Try exact raycast against the target's bounding box
        Optional<Vec3d> exact = box.raycast(eye, end);

        Vec3d h;
        if (exact.isPresent()) {
            h = exact.get();
        } else {
            // Ray missed then find the closest point on look ray to the target center and clamp onto the bounding box
            Vec3d toCenter = new Vec3d(cx - eye.x, (box.minY + box.maxY) * 0.5 - eye.y, cz - eye.z);
            double t = Math.max(0.0, Math.min(toCenter.dotProduct(look), 6.0));
            Vec3d closest = eye.add(look.multiply(t));
            h = new Vec3d(
                    Math.max(box.minX, Math.min(box.maxX, closest.x)),
                    Math.max(box.minY, Math.min(box.maxY, closest.y)),
                    Math.max(box.minZ, Math.min(box.maxZ, closest.z))
            );
        }

        // Compute yNorm with eye-height normalization
        double py = Math.max(box.minY, Math.min(box.maxY, h.y));
        double yRaw = Math.max(0.0, Math.min(1.0, (py - box.minY) / height));
        double headStart = Math.max(0.0, Math.min(0.99,
                (living.getEyeY() - box.minY) / height));
        final double HEAD_BAND_START = 0.88;
        double yNorm;
        if (yRaw <= headStart) {
            yNorm = (headStart > 1e-6) ? (yRaw / headStart) * HEAD_BAND_START : 0.0;
        } else {
            yNorm = HEAD_BAND_START + ((yRaw - headStart) / (1.0 - headStart)) * (1.0 - HEAD_BAND_START);
        }
        yNorm = Math.max(0.0, Math.min(1.0, yNorm));

        // Local-space X/Z relative to the target's body yaw
        double yawRad = Math.toRadians(living.getBodyYaw());
        Vec3d forward = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3d right   = new Vec3d(forward.z, 0, -forward.x);
        Vec3d offset  = new Vec3d(h.x - cx, 0, h.z - cz);

        double xNorm = Math.max(-1.0, Math.min(1.0, offset.dotProduct(right)   / halfWidth));
        double zNorm = Math.max(-1.0, Math.min(1.0, offset.dotProduct(forward) / halfWidth));

        HitLocationTracker.record(living, xNorm, yNorm, zNorm);
    }
}