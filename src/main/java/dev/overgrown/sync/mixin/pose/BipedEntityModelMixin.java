package dev.overgrown.sync.mixin.pose;

import dev.overgrown.sync.factory.power.type.pose.PosePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Optional;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

    @Shadow
    public BipedEntityModel.ArmPose leftArmPose;

    @Shadow
    public BipedEntityModel.ArmPose rightArmPose;

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(
                    "HEAD"
            )
    )
    private void sync$applyArmPose(T entity, float limbAngle, float limbDistance,
                                   float animationProgress, float headYaw, float headPitch,
                                   CallbackInfo ci) {
        Optional<PosePower> power = PowerHolderComponent.getPowers(entity, PosePower.class).stream()
                .max(Comparator.comparing(PosePower::getPriority));

        power.ifPresent(p -> {
            BipedEntityModel.ArmPose armPose = p.getArmPose();
            if (armPose != null) {
                this.leftArmPose = armPose;
                this.rightArmPose = armPose;
            }
        });
    }
}