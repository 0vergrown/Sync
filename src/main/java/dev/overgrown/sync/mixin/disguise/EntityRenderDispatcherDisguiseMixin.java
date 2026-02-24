package dev.overgrown.sync.mixin.disguise;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.factory.disguise.DisguiseData;
import dev.overgrown.sync.factory.disguise.client.ClientDisguiseManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts {@link EntityRenderDispatcher#getRenderer(Entity)} so that a
 * disguised entity is rendered using the target entity type's renderer.
 *
 * <p>Player-to-player disguises are handled purely via the
 * {@link PlayerListEntryDisguiseMixin} (skin/model swap), so we only touch
 * non-player targets here.
 */
@Mixin(value = EntityRenderDispatcher.class, priority = 900)
public abstract class EntityRenderDispatcherDisguiseMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "getRenderer",
            at = @At(
                    "HEAD"
            ),cancellable = true
    )
    public <T extends Entity> void sync$getDisguisedRenderer(
            T entity,
            CallbackInfoReturnable<EntityRenderer<? super T>> cir) {

        if (entity instanceof CloneEntity) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (entity == client.player) return;

        DisguiseData disguise = ClientDisguiseManager.getDisguise(entity.getId());
        if (disguise == null) return;

        if (entity instanceof PlayerEntity && disguise.isPlayerDisguise()) return;

        Entity dummy = ClientDisguiseManager.getSyncedDummy(entity.getId(), entity);
        if (dummy == null) return;

        try {
            EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
            EntityRenderer dummyRenderer = dispatcher.getRenderer(dummy);
            if (dummyRenderer != null) {
                cir.setReturnValue((EntityRenderer<? super T>) dummyRenderer);
            }
        } catch (Exception e) {
            Sync.LOGGER.warn("sync:disguise – could not resolve dummy renderer (type={}): {}",
                    disguise.getTargetEntityTypeId(), e.getMessage());
        }
    }

    /**
     * Redirect the actual render call to use the dummy entity instead of the
     * real entity. The entity renderer must only ever receive its own entity, never a player entity.
     */
    @Inject(
            method = "render",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private <E extends Entity> void sync$renderDisguised(
            E entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (entity == client.player) return;

        DisguiseData disguise = ClientDisguiseManager.getDisguise(entity.getId());
        if (disguise == null || disguise.isPlayerDisguise()) return;

        Entity dummy = ClientDisguiseManager.getSyncedDummy(entity.getId(), entity);
        if (dummy == null) return;

        // Render the dummy correctly typed for its renderer.
        // This call re-enters render() but dummy has no disguise entry so it
        // falls through to vanilla rendering.
        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
        dispatcher.render(dummy, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
        ci.cancel();
    }

    @Inject(
            method = "shouldRender",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private <E extends Entity> void sync$shouldRenderDisguised(
            E entity, Frustum frustum, double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (entity == client.player) return;

        Entity dummy = ClientDisguiseManager.getSyncedDummy(entity.getId(), entity);
        if (dummy == null) return;

        @SuppressWarnings("unchecked")
        EntityRenderer<Entity> renderer =
                (EntityRenderer<Entity>) ((EntityRenderDispatcher) (Object) this).getRenderer(dummy);
        cir.setReturnValue(renderer.shouldRender(dummy, frustum, x, y, z));
    }
}