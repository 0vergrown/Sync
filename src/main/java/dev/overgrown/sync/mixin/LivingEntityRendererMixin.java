package dev.overgrown.sync.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.ModelFlipPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "shouldFlipUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void sync$flipUpsideDown(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (PowerHolderComponent.hasPower(livingEntity, ModelFlipPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isUsingRiptide()Z"))
    private boolean sync$inRiptidePose(boolean original, LivingEntity entity) {
        return original || entity.isInPose(EntityPose.SPIN_ATTACK);
    }

}