package dev.overgrown.sync.mixin;

import dev.overgrown.sync.factory.power.type.FlipModelPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow public abstract Camera getCamera();

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", shift = At.Shift.AFTER))
    private void voile$flipView(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        Entity entity = this.getCamera().getFocusedEntity();
        if (entity == null) return;

        String string = Formatting.strip(entity.getName().getString());
        // Don't flip the entity's view if they would be flipped the right way up due to their name
        if ("Dinnerbone".equals(string) || "Grumm".equals(string)) return;

        List<FlipModelPower> powers = PowerHolderComponent.getPowers(entity, FlipModelPower.class);

        if (powers.stream().anyMatch(FlipModelPower::shouldFlipView)) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));
        }
    }
}