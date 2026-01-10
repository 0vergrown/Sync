package dev.overgrown.sync.mixin;

import dev.overgrown.sync.factory.power.type.ModifyModelPartsPower;
import dev.overgrown.sync.utils.ModelPartTransformation;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(BipedEntityModel.class)
public abstract class ModifyModelPartsBipedEntityModelMixin<T extends LivingEntity>
        extends AnimalModel<T>
        implements ModelWithArms,
        ModelWithHead {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart leftLeg;
    @Shadow @Final public ModelPart rightLeg;

    @Unique
    private static final Map<UUID, Map<String, Float>> SYNC$ORIGINAL_VALUES = new HashMap<>();
    @Unique
    private static final Map<UUID, Boolean> SYNC$HAS_POWER = new HashMap<>();

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(value = "HEAD")
    )
    public void setAnglesHead(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        UUID entityId = livingEntity.getUuid();
        boolean hasPower = PowerHolderComponent.hasPower(livingEntity, ModifyModelPartsPower.class);
        Boolean hadPower = SYNC$HAS_POWER.get(entityId);

        if (hasPower) {
            // Store original values when first gaining power
            if (!SYNC$ORIGINAL_VALUES.containsKey(entityId)) {
                storeOriginalValues(entityId);
            }

            // Reset to original values before normal model animations
            if (hadPower == null || hadPower) { // Only reset if they had power last frame or just gained it
                restoreOriginalValues(entityId);
            }
        } else if (hadPower != null && hadPower) {
            // If just lost the power then reset to original values
            restoreOriginalValues(entityId);
            SYNC$ORIGINAL_VALUES.remove(entityId);
        }

        SYNC$HAS_POWER.put(entityId, hasPower);
    }

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(value = "TAIL")
    )
    public void setAnglesTail(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!PowerHolderComponent.hasPower(livingEntity, ModifyModelPartsPower.class)) {
            return;
        }

        // Apply transformations after all normal animations
        List<ModelPartTransformation> transformations = new ArrayList<>();
        PowerHolderComponent.getPowers(livingEntity, ModifyModelPartsPower.class).forEach(power -> {
            transformations.addAll(power.getTransformations());
        });

        for (ModelPartTransformation t : transformations) {
            applyTransformation(t);
        }
    }

    @Unique
    private void storeOriginalValues(UUID entityId) {
        Map<String, Float> originals = new HashMap<>();

        // Store default values for each part
        storePartValues(originals, head, "head");
        storePartValues(originals, hat, "hat");
        storePartValues(originals, body, "body");
        storePartValues(originals, rightArm, "rightarm");
        storePartValues(originals, leftArm, "leftarm");
        storePartValues(originals, rightLeg, "rightleg");
        storePartValues(originals, leftLeg, "leftleg");

        SYNC$ORIGINAL_VALUES.put(entityId, originals);
    }

    @Unique
    private void storePartValues(Map<String, Float> originals, ModelPart part, String partName) {
        if (part == null) return;

        originals.put(partName + "_pivotX", part.getDefaultTransform().pivotX);
        originals.put(partName + "_pivotY", part.getDefaultTransform().pivotY);
        originals.put(partName + "_pivotZ", part.getDefaultTransform().pivotZ);
        originals.put(partName + "_pitch", part.getDefaultTransform().pitch);
        originals.put(partName + "_yaw", part.getDefaultTransform().yaw);
        originals.put(partName + "_roll", part.getDefaultTransform().roll);
        // Scale defaults are always 1.0, not stored in defaultTransform
        originals.put(partName + "_xScale", 1.0f);
        originals.put(partName + "_yScale", 1.0f);
        originals.put(partName + "_zScale", 1.0f);
        // Visible/hidden defaults
        originals.put(partName + "_visible", 1.0f); // 1 = true
        originals.put(partName + "_hidden", 0.0f);  // 0 = false
    }

    @Unique
    private void restoreOriginalValues(UUID entityId) {
        Map<String, Float> originals = SYNC$ORIGINAL_VALUES.get(entityId);
        if (originals == null) return;

        restorePartValues(head, "head", originals);
        restorePartValues(hat, "hat", originals);
        restorePartValues(body, "body", originals);
        restorePartValues(rightArm, "rightarm", originals);
        restorePartValues(leftArm, "leftarm", originals);
        restorePartValues(rightLeg, "rightleg", originals);
        restorePartValues(leftLeg, "leftleg", originals);
    }

    @Unique
    private void restorePartValues(ModelPart part, String partName, Map<String, Float> originals) {
        if (part == null) return;

        String prefix = partName + "_";
        part.pivotX = originals.getOrDefault(prefix + "pivotX", part.getDefaultTransform().pivotX);
        part.pivotY = originals.getOrDefault(prefix + "pivotY", part.getDefaultTransform().pivotY);
        part.pivotZ = originals.getOrDefault(prefix + "pivotZ", part.getDefaultTransform().pivotZ);
        part.pitch = originals.getOrDefault(prefix + "pitch", part.getDefaultTransform().pitch);
        part.yaw = originals.getOrDefault(prefix + "yaw", part.getDefaultTransform().yaw);
        part.roll = originals.getOrDefault(prefix + "roll", part.getDefaultTransform().roll);
        part.xScale = originals.getOrDefault(prefix + "xScale", 1.0f);
        part.yScale = originals.getOrDefault(prefix + "yScale", 1.0f);
        part.zScale = originals.getOrDefault(prefix + "zScale", 1.0f);

        Float visible = originals.get(prefix + "visible");
        if (visible != null) {
            part.visible = visible != 0;
        }
        Float hidden = originals.get(prefix + "hidden");
        if (hidden != null) {
            part.hidden = hidden != 0;
        }
    }

    @Unique
    private ModelPart getModelPart(String partName) {
        switch (partName.toLowerCase()) {
            case "head": return head;
            case "hat": return hat;
            case "body": return body;
            case "rightarm": return rightArm;
            case "leftarm": return leftArm;
            case "rightleg": return rightLeg;
            case "leftleg": return leftLeg;
            default: return null;
        }
    }

    @Unique
    private void applyTransformation(ModelPartTransformation t) {
        ModelPart targetPart = getModelPart(t.getModelPart());
        if (targetPart == null) return;

        String type = t.getType().toLowerCase();
        float value = t.getValue();

        // Get original value from storage
        UUID entityId = null;
        Map<String, Float> originals = null;
        if (SYNC$ORIGINAL_VALUES.size() == 1) {
            // If only one entity has power, use its values
            entityId = SYNC$ORIGINAL_VALUES.keySet().iterator().next();
            originals = SYNC$ORIGINAL_VALUES.get(entityId);
        }

        switch (type) {
            case "pitch":
                targetPart.pitch += value;
                break;
            case "yaw":
                targetPart.yaw += value;
                break;
            case "roll":
                targetPart.roll += value;
                break;
            case "visible":
                targetPart.visible = value != 0;
                break;
            case "hidden":
                targetPart.hidden = value != 0;
                break;
            case "xscale":
                if (originals != null) {
                    // Get original scale from storage
                    float original = originals.getOrDefault(t.getModelPart().toLowerCase() + "_xScale", 1.0f);
                    targetPart.xScale = original + value;
                } else {
                    // Fallback: assume original is 1
                    targetPart.xScale = 1.0f + value;
                }
                break;
            case "yscale":
                if (originals != null) {
                    float original = originals.getOrDefault(t.getModelPart().toLowerCase() + "_yScale", 1.0f);
                    targetPart.yScale = original + value;
                } else {
                    targetPart.yScale = 1.0f + value;
                }
                break;
            case "zscale":
                if (originals != null) {
                    float original = originals.getOrDefault(t.getModelPart().toLowerCase() + "_zScale", 1.0f);
                    targetPart.zScale = original + value;
                } else {
                    targetPart.zScale = 1.0f + value;
                }
                break;
            case "pivotx":
                targetPart.pivotX += value;
                break;
            case "pivoty":
                targetPart.pivotY += value;
                break;
            case "pivotz":
                targetPart.pivotZ += value;
                break;
        }
    }
}