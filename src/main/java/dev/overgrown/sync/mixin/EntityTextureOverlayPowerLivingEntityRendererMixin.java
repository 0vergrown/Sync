package dev.overgrown.sync.mixin;

import dev.overgrown.sync.factory.power.type.entity_texture_overlay.client.render.EntityTextureOverlayFeatureRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityTextureOverlayPowerLivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected abstract boolean addFeature(FeatureRenderer<T, M> feature);

    @Final
    @Shadow protected List<FeatureRenderer<T, M>> features;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sync$addOverlayRenderer(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
        // Add our overlay feature renderer to all living entity renderers
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, M> context = (LivingEntityRenderer<T, M>) (Object) this;
        this.addFeature(new EntityTextureOverlayFeatureRenderer<>(context));
    }
}