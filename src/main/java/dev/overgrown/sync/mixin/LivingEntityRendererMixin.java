package dev.overgrown.sync.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.FlipModelPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "shouldFlipUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void voile$flipModel(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!PowerHolderComponent.hasPower(entity, FlipModelPower.class)) return;

        String string = Formatting.strip(entity.getName().getString());
        if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
            // If the entity would already be flipped due to its name, flip it back
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isUsingRiptide()Z"))
    private boolean sync$inRiptidePose(boolean original, LivingEntity entity) {
        return original || entity.isInPose(EntityPose.SPIN_ATTACK);
    }

}