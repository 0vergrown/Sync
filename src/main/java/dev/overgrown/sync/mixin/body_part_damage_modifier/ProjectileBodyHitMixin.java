package dev.overgrown.sync.mixin.body_part_damage_modifier;

import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.HitLocationTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class ProjectileBodyHitMixin {

    @Inject(
            method = "onEntityHit",
            at = @At(
                    "HEAD"
            )
    )
    private void sync$recordHit(EntityHitResult result, CallbackInfo ci) {
        if (result == null) return;
        Entity hit = result.getEntity();
        if (!(hit instanceof LivingEntity living)) return;
        if (living.getWorld().isClient) return;

        Entity self = (Entity) (Object) this;
        Box box = living.getBoundingBox();
        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        double height = Math.max(box.maxY - box.minY, 1e-3);
        double halfWidth = Math.max(living.getWidth() * 0.5, 1e-3);

        // Use the projectile's actual world position since it IS physically at the impact point when onEntityHit fires.
        Vec3d impact = self.getPos();

        double py = Math.max(box.minY, Math.min(box.maxY, impact.y));

        // Eye-height-aware yNorm so the headband aligns correctly regardless of pose (standing, swimming, crouching).
        double yRaw = Math.max(0.0, Math.min(1.0, (py - box.minY) / height));
        double headStart = Math.max(0.0, Math.min(0.99,
                (living.getEyeY() - box.minY) / height));
        final double HEAD_BAND_START = 0.88;
        double yNorm;
        if (yRaw <= headStart) {
            yNorm = (headStart > 1e-6)
                    ? (yRaw / headStart) * HEAD_BAND_START
                    : 0.0;
        } else {
            yNorm = HEAD_BAND_START
                    + ((yRaw - headStart) / (1.0 - headStart))
                    * (1.0 - HEAD_BAND_START);
        }
        yNorm = Math.max(0.0, Math.min(1.0, yNorm));

        // Local-space axes relative to the entity's body yaw
        double yawRad = Math.toRadians(living.getBodyYaw());
        Vec3d forward = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3d right   = new Vec3d(forward.z, 0, -forward.x);
        Vec3d offset  = new Vec3d(impact.x - cx, 0, impact.z - cz);

        double xNorm = Math.max(-1.0, Math.min(1.0, offset.dotProduct(right)   / halfWidth));
        double zNorm = Math.max(-1.0, Math.min(1.0, offset.dotProduct(forward) / halfWidth));

        HitLocationTracker.record(living, xNorm, yNorm, zNorm);
    }
}