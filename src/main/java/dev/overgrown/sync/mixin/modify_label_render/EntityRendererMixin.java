package dev.overgrown.sync.mixin.modify_label_render;

import dev.overgrown.sync.power.type.modify_label_render.ModifyLabelRenderPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.Prioritized;
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

    private static ModifyLabelRenderPowerType sync$findApplicablePower(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return null;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return null;

        Entity viewer = client.getCameraEntity();
        if (viewer == null) return null;

        if (PowerHolderComponent.KEY.maybeGet(living).isEmpty()) return null;

        Prioritized.CallInstance<ModifyLabelRenderPowerType> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(living, ModifyLabelRenderPowerType.class,
            power -> power.isActive() && power.shouldApplyForViewer(viewer));

        if (callInstance.isEmpty()) return null;
        var powers = callInstance.getPowerTypes(callInstance.getMaxPriority());
        return powers.isEmpty() ? null : powers.get(0);
    }

    @Inject(
        method = "renderLabelIfPresent",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderLabelHead(T entity, Text text, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light,
                                   float tickDelta, CallbackInfo ci) {
        ModifyLabelRenderPowerType power = sync$findApplicablePower(entity);
        if (power == null) return;
        if (power.getRenderMode() == ModifyLabelRenderPowerType.RenderMode.HIDE_COMPLETELY) {
            ci.cancel();
        }
    }

    @ModifyVariable(
        method = "renderLabelIfPresent",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Text modifyLabelText(Text originalText, T entity, Text text, MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        ModifyLabelRenderPowerType power = sync$findApplicablePower(entity);
        if (power == null) return originalText;
        Text modifiedText = power.getModifiedText();
        return modifiedText != null ? modifiedText : originalText;
    }
}
