package dev.overgrown.sync.mixin.pose;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @ModifyExpressionValue(
            method = "setupTransforms",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isUsingRiptide()Z"
            )
    )
    private boolean sync$inRiptidePose(boolean original, LivingEntity entity) {
        return original || entity.isInPose(EntityPose.SPIN_ATTACK);
    }

}