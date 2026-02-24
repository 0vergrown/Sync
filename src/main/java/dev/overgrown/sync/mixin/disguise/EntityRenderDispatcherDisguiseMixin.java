package dev.overgrown.sync.mixin.disguise;

import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.factory.disguise.DisguiseData;
import dev.overgrown.sync.factory.disguise.client.ClientDisguiseManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderDispatcher.class, priority = 900)
public abstract class EntityRenderDispatcherDisguiseMixin {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity> void sync$renderDisguised(
            E entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci) {

        if (entity instanceof CloneEntity) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (entity == client.player && client.options.getPerspective().isFirstPerson()) return;

        DisguiseData disguise = ClientDisguiseManager.getDisguise(entity.getId());
        if (disguise == null || disguise.isPlayerDisguise()) return;

        Entity dummy = ClientDisguiseManager.getSyncedDummy(entity.getId(), entity);
        if (dummy == null) return;

        // -----------------------------------------------------------------
        // Sync animation state from the real entity to the dummy so that:
        //   • Walk/run animations play correctly (limbAngle / limbDistance)
        //   • The head tracks where the player is looking (headYaw / pitch)
        //   • Body rotation matches movement direction (bodyYaw)
        //
        // getSyncedDummy only copies world position; it does not copy any of
        // the per-tick animation fields, so without this block the dummy
        // stands completely still and its head snaps to a default orientation.
        // -----------------------------------------------------------------
        dummy.setYaw(entity.getYaw());
        dummy.setPitch(entity.getPitch());
        dummy.prevYaw   = entity.prevYaw;
        dummy.prevPitch = entity.prevPitch;

        if (entity instanceof LivingEntity realLiving && dummy instanceof LivingEntity dummyLiving) {
            // Head yaw - use the Entity-level setter/getter so we never touch
            // a field that might not be accessible from this mixin's target class.
            // LivingEntity overrides both methods to read/write its own headYaw field.
            dummyLiving.setHeadYaw(realLiving.getHeadYaw());

            // Body yaw - same approach. setBodyYaw is a no-op on base Entity but LivingEntity overrides it to write bodyYaw.
            dummyLiving.setBodyYaw(realLiving.getBodyYaw());

            // Limb (walk/run) animation.
            // Since there's three raw fields (limbAngle / prevLimbAngle / limbDistance) were replaced by a LimbAnimator helper.
            // Copy its internal speed and position directly so the renderer sees the correct interpolated values without having to call updateLimbs() ourselves.
            dummyLiving.limbAnimator.setSpeed(realLiving.limbAnimator.getSpeed());
            dummyLiving.limbAnimator.getPos(realLiving.limbAnimator.getPos());

            // Hand/arm swing animation
            dummyLiving.handSwingProgress     = realLiving.handSwingProgress;
            dummyLiving.lastHandSwingProgress = realLiving.lastHandSwingProgress;
        }

        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
        dispatcher.render(dummy, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
        ci.cancel();
    }
}