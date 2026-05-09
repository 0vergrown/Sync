package dev.overgrown.sync.mixin.energy_swirl;

import dev.overgrown.sync.factory.power.type.energy_swirl.client.render.feature.EnergySwirlFeatureRenderer;
import dev.overgrown.sync.mixin.energy_swirl.accessor.LivingEntityRendererAccessor;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addEnergySwirlFeature(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
        LivingEntityRendererAccessor<T, M> accessor = (LivingEntityRendererAccessor<T, M>) this;
        accessor.invokeAddFeature(new EnergySwirlFeatureRenderer<>((LivingEntityRenderer<T, M>) (Object) this));
    }
}