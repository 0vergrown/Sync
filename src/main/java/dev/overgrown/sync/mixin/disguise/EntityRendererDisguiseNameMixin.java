package dev.overgrown.sync.mixin.disguise;

import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.client.ClientDisguiseManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Replaces the label text shown above a disguised entity with the disguise
 * target's display name.
 *
 * <p>Applies to every entity (not only players) so that mob-to-mob disguises
 * also show the correct name.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererDisguiseNameMixin<T extends Entity> {

    /**
     * Modify the {@code text} parameter of {@code renderLabelIfPresent(T, Text, MatrixStack, VertexConsumerProvider, int)}.
     */
    @ModifyVariable(
            method = "renderLabelIfPresent",
            at = @At(
                    "HEAD"
            ),
            argsOnly = true
    )
    private Text sync$modifyDisguiseLabel(
            Text originalText,
            T entity,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light) {

        DisguiseData disguise = ClientDisguiseManager.getDisguise(entity.getId());
        if (disguise != null) {
            return disguise.getTargetDisplayName();
        }
        return originalText;
    }
}