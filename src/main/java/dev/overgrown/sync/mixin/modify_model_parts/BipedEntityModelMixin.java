package dev.overgrown.sync.mixin.modify_model_parts;

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
public abstract class BipedEntityModelMixin<T extends LivingEntity>
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
    private final Map<String, Float> SYNC$ORIGINAL_VALUES = new HashMap<>();
    @Unique
    private boolean SYNC$HAS_POWER = false;

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "HEAD"
            )
    )
    public void setAnglesHead(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        boolean hasPower = PowerHolderComponent.hasPower(livingEntity, ModifyModelPartsPower.class);

        if (hasPower) {
            // Store original values when first gaining power
            if (SYNC$ORIGINAL_VALUES.isEmpty()) {
                storeOriginalValues();
            }

            // Always reset to original values before applying transformations
            restoreOriginalValues();
        } else if (SYNC$HAS_POWER) {
            // If just lost the power then reset to original values
            restoreOriginalValues();
            SYNC$ORIGINAL_VALUES.clear();
        }

        SYNC$HAS_POWER = hasPower;
    }

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "TAIL"
            )
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
    private void storeOriginalValues() {
        // Store current values for each part
        storePartValues(head, "head");
        storePartValues(hat, "hat");
        storePartValues(body, "body");
        storePartValues(rightArm, "rightarm");
        storePartValues(leftArm, "leftarm");
        storePartValues(rightLeg, "rightleg");
        storePartValues(leftLeg, "leftleg");
    }

    @Unique
    private void storePartValues(ModelPart part, String partName) {
        if (part == null) return;

        String prefix = partName + "_";
        SYNC$ORIGINAL_VALUES.put(prefix + "pivotX", part.pivotX);
        SYNC$ORIGINAL_VALUES.put(prefix + "pivotY", part.pivotY);
        SYNC$ORIGINAL_VALUES.put(prefix + "pivotZ", part.pivotZ);
        SYNC$ORIGINAL_VALUES.put(prefix + "pitch", part.pitch);
        SYNC$ORIGINAL_VALUES.put(prefix + "yaw", part.yaw);
        SYNC$ORIGINAL_VALUES.put(prefix + "roll", part.roll);
        SYNC$ORIGINAL_VALUES.put(prefix + "xScale", part.xScale);
        SYNC$ORIGINAL_VALUES.put(prefix + "yScale", part.yScale);
        SYNC$ORIGINAL_VALUES.put(prefix + "zScale", part.zScale);
        SYNC$ORIGINAL_VALUES.put(prefix + "visible", part.visible ? 1.0f : 0.0f);
        SYNC$ORIGINAL_VALUES.put(prefix + "hidden", part.hidden ? 1.0f : 0.0f);
    }

    @Unique
    private void restoreOriginalValues() {
        if (SYNC$ORIGINAL_VALUES.isEmpty()) return;

        restorePartValues(head, "head");
        restorePartValues(hat, "hat");
        restorePartValues(body, "body");
        restorePartValues(rightArm, "rightarm");
        restorePartValues(leftArm, "leftarm");
        restorePartValues(rightLeg, "rightleg");
        restorePartValues(leftLeg, "leftleg");
    }

    @Unique
    private void restorePartValues(ModelPart part, String partName) {
        if (part == null) return;

        String prefix = partName + "_";
        part.pivotX = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "pivotX", part.pivotX);
        part.pivotY = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "pivotY", part.pivotY);
        part.pivotZ = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "pivotZ", part.pivotZ);
        part.pitch = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "pitch", part.pitch);
        part.yaw = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "yaw", part.yaw);
        part.roll = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "roll", part.roll);
        part.xScale = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "xScale", 1.0f);
        part.yScale = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "yScale", 1.0f);
        part.zScale = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "zScale", 1.0f);

        Float visible = SYNC$ORIGINAL_VALUES.get(prefix + "visible");
        if (visible != null) {
            part.visible = visible != 0;
        }
        Float hidden = SYNC$ORIGINAL_VALUES.get(prefix + "hidden");
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
                // Get original value from storage
                float originalX = SYNC$ORIGINAL_VALUES.getOrDefault(t.getModelPart().toLowerCase() + "_xScale", 1.0f);
                targetPart.xScale = originalX + value;
                break;
            case "yscale":
                float originalY = SYNC$ORIGINAL_VALUES.getOrDefault(t.getModelPart().toLowerCase() + "_yScale", 1.0f);
                targetPart.yScale = originalY + value;
                break;
            case "zscale":
                float originalZ = SYNC$ORIGINAL_VALUES.getOrDefault(t.getModelPart().toLowerCase() + "_zScale", 1.0f);
                targetPart.zScale = originalZ + value;
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