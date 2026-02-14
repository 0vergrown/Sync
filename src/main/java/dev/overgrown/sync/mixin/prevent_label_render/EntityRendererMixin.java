package dev.overgrown.sync.mixin.prevent_label_render;

import dev.overgrown.sync.factory.power.type.prevent_label_render.PreventLabelRenderPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(T entity, Text text, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light,
                               CallbackInfo ci) {
        // Only living entities can have powers
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // Get the viewer (the player through whose eyes we are rendering)
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Entity viewer = client.getCameraEntity();
        if (viewer == null) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.get(living);
        if (component == null) {
            return;
        }

        for (PreventLabelRenderPower power : component.getPowers(PreventLabelRenderPower.class)) {
            // Check both the power's own condition and the viewer conditions
            if (power.isActive() && power.shouldHideForViewer(viewer)) {
                ci.cancel();
                return;
            }
        }
    }
}