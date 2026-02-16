package dev.overgrown.sync.mixin.modify_label_render;

import dev.overgrown.sync.factory.power.type.modify_label_render.ModifyLabelRenderPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Prioritized;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(
            method = "renderLabelIfPresent",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void onRenderLabelHead(T entity, Text text, MatrixStack matrices,
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

        // Get the highest priority power that applies for this viewer
        Prioritized.CallInstance<ModifyLabelRenderPower> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(living, ModifyLabelRenderPower.class,
                power -> power.isActive() && power.shouldApplyForViewer(viewer));

        if (!callInstance.hasPowers(callInstance.getMaxPriority())) {
            return;
        }

        // Get the highest priority power
        ModifyLabelRenderPower power = callInstance.getPowers(callInstance.getMaxPriority()).get(0);

        // Handle render mode
        if (power.getRenderMode() == ModifyLabelRenderPower.RenderMode.HIDE_COMPLETELY) {
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "renderLabelIfPresent",
            at = @At(
                    "HEAD"
            ),
            argsOnly = true
    )
    private Text modifyLabelText(Text originalText, T entity, Text text, MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers, int light) {
        // Only living entities can have powers
        if (!(entity instanceof LivingEntity living)) {
            return originalText;
        }

        // Get the viewer
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return originalText;
        }

        Entity viewer = client.getCameraEntity();
        if (viewer == null) {
            return originalText;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.get(living);
        if (component == null) {
            return originalText;
        }

        // Get the highest priority power that applies for this viewer
        Prioritized.CallInstance<ModifyLabelRenderPower> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(living, ModifyLabelRenderPower.class,
                power -> power.isActive() && power.shouldApplyForViewer(viewer));

        if (!callInstance.hasPowers(callInstance.getMaxPriority())) {
            return originalText;
        }

        // Get the highest priority power
        ModifyLabelRenderPower power = callInstance.getPowers(callInstance.getMaxPriority()).get(0);

        // Replace text if provided
        Text modifiedText = power.getModifiedText();
        if (modifiedText != null) {
            return modifiedText;
        }

        return originalText;
    }

    @ModifyVariable(
            method = "renderLabelIfPresent",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private boolean modifyLabelVisibility(boolean shouldRender, T entity, Text text, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light) {
        // Only living entities can have powers
        if (!(entity instanceof LivingEntity living)) {
            return shouldRender;
        }

        // Get the viewer
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return shouldRender;
        }

        Entity viewer = client.getCameraEntity();
        if (viewer == null) {
            return shouldRender;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.get(living);
        if (component == null) {
            return shouldRender;
        }

        // Get the highest priority power that applies for this viewer
        Prioritized.CallInstance<ModifyLabelRenderPower> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(living, ModifyLabelRenderPower.class,
                power -> power.isActive() && power.shouldApplyForViewer(viewer));

        if (!callInstance.hasPowers(callInstance.getMaxPriority())) {
            return shouldRender;
        }

        // Get the highest priority power
        ModifyLabelRenderPower power = callInstance.getPowers(callInstance.getMaxPriority()).get(0);

        // If render mode is hide_partially, force it to render like sneaking (semi-transparent)
        if (power.getRenderMode() == ModifyLabelRenderPower.RenderMode.HIDE_PARTIALLY) {
            return false;
        }

        return shouldRender;
    }
}