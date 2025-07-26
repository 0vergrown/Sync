package dev.overgrown.sync.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Unique
    private int sync$pseudoRoll = 0;

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isFallFlying()Z"))
    private boolean sync$inFallFlyingPose(boolean original, AbstractClientPlayerEntity player, @Share("applyPseudoRoll") LocalBooleanRef applyPseudoRollRef) {

        if (original || player.isInPose(EntityPose.FALL_FLYING)) {

            if (player.isInPose(EntityPose.FALL_FLYING)) {
                applyPseudoRollRef.set(true);
                ++sync$pseudoRoll;
            }

            return true;

        }

        this.sync$pseudoRoll = 0;
        return false;

    }

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getRoll()I"))
    private int sync$applyPseudoRoll(int original, AbstractClientPlayerEntity player, @Share("applyPseudoRoll") LocalBooleanRef applyPseudoRollRef) {
        return applyPseudoRollRef.get()
                ? sync$pseudoRoll
                : original;
    }

}