package dev.overgrown.sync.mixin.flip_model;

import dev.overgrown.sync.power.type.flip_model.FlipModelPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Flips the player's camera view 180° around the Z-axis when an active
 * {@link FlipModelPowerType} on the focused entity has {@code flip_view = true}.
 *
 * <p>1.21 port: in 1.20.1 the {@code matrices} parameter could be modified
 * directly because it was passed into {@code renderWorld}. In 1.21 the local
 * {@code MatrixStack} is created inside {@code renderWorld} and folded into the
 * projection {@code Matrix4f} before {@code loadProjectionMatrix} is called.
 * We rotate that final projection matrix instead via {@code @ModifyArg}.</p>
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow public abstract Camera getCamera();

    @ModifyArg(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/GameRenderer;loadProjectionMatrix(Lorg/joml/Matrix4f;)V"
        )
    )
    private Matrix4f sync$flipView(Matrix4f matrix) {
        Entity entity = this.getCamera().getFocusedEntity();
        if (entity == null) return matrix;

        String name = Formatting.strip(entity.getName().getString());
        if ("Dinnerbone".equals(name) || "Grumm".equals(name)) return matrix;

        List<FlipModelPowerType> powers =
            PowerHolderComponent.getPowerTypes(entity, FlipModelPowerType.class);
        if (powers.stream().anyMatch(FlipModelPowerType::shouldFlipView)) {
            matrix.rotate((float) Math.PI, new Vector3f(0f, 0f, 1f));
        }
        return matrix;
    }
}
