package dev.overgrown.sync.mixin.disguise;

import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.client.ClientDisguiseManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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

        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
        dispatcher.render(dummy, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
        ci.cancel();
    }
}
