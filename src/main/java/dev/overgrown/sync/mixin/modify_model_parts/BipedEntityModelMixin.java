package dev.overgrown.sync.mixin.modify_model_parts;

import dev.overgrown.sync.power.type.modify_model_parts.ModifyModelPartsPowerType;
import dev.overgrown.sync.power.type.modify_model_parts.util.ModelPartTransformation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity>
    extends AnimalModel<T>
    implements ModelWithArms, ModelWithHead {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart leftLeg;
    @Shadow @Final public ModelPart rightLeg;

    @Unique private final Map<String, Float> SYNC$ORIGINAL_VALUES = new HashMap<>();
    @Unique private final Map<String, Float> SYNC$LOCKED_VALUES = new HashMap<>();
    @Unique private final Set<String> SYNC$OVERRIDE_ANIMATION_PARTS = new HashSet<>();
    @Unique private boolean SYNC$HAS_POWER = false;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    public void setAnglesHead(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        boolean hasPower = !PowerHolderComponent.getPowerTypes(livingEntity, ModifyModelPartsPowerType.class).isEmpty();

        if (hasPower) {
            if (SYNC$ORIGINAL_VALUES.isEmpty()) {
                storeOriginalValues();
            }

            restoreOriginalValues();

            SYNC$LOCKED_VALUES.clear();
            SYNC$OVERRIDE_ANIMATION_PARTS.clear();
        } else if (SYNC$HAS_POWER) {
            restoreOriginalValues();
            SYNC$ORIGINAL_VALUES.clear();
            SYNC$LOCKED_VALUES.clear();
            SYNC$OVERRIDE_ANIMATION_PARTS.clear();
        }

        SYNC$HAS_POWER = hasPower;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void setAnglesTail(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        List<ModifyModelPartsPowerType> powers =
            PowerHolderComponent.getPowerTypes(livingEntity, ModifyModelPartsPowerType.class);
        if (powers.isEmpty()) return;

        List<ModelPartTransformation> transformations = new ArrayList<>();
        powers.forEach(p -> transformations.addAll(p.getTransformations()));

        for (ModelPartTransformation t : transformations) {
            if (t.getOverrideAnimation()) {
                String partName = t.getModelPart().toLowerCase();
                String type = t.getType().toLowerCase();
                SYNC$OVERRIDE_ANIMATION_PARTS.add(partName + "_" + type);
            }
        }

        for (ModelPartTransformation t : transformations) {
            applyTransformation(t);
        }
    }

    @Unique
    private void storeOriginalValues() {
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

        String pitchKey = partName + "_pitch";
        String yawKey = partName + "_yaw";
        String rollKey = partName + "_roll";

        if (!SYNC$OVERRIDE_ANIMATION_PARTS.contains(pitchKey)) {
            part.pitch = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "pitch", part.pitch);
        }
        if (!SYNC$OVERRIDE_ANIMATION_PARTS.contains(yawKey)) {
            part.yaw = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "yaw", part.yaw);
        }
        if (!SYNC$OVERRIDE_ANIMATION_PARTS.contains(rollKey)) {
            part.roll = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "roll", part.roll);
        }

        part.xScale = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "xScale", 1.0f);
        part.yScale = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "yScale", 1.0f);
        part.zScale = SYNC$ORIGINAL_VALUES.getOrDefault(prefix + "zScale", 1.0f);

        Float visible = SYNC$ORIGINAL_VALUES.get(prefix + "visible");
        if (visible != null) part.visible = visible != 0;
        Float hidden = SYNC$ORIGINAL_VALUES.get(prefix + "hidden");
        if (hidden != null) part.hidden = hidden != 0;
    }

    @Unique
    private ModelPart getModelPart(String partName) {
        return switch (partName.toLowerCase()) {
            case "head" -> head;
            case "hat" -> hat;
            case "body" -> body;
            case "rightarm" -> rightArm;
            case "leftarm" -> leftArm;
            case "rightleg" -> rightLeg;
            case "leftleg" -> leftLeg;
            default -> null;
        };
    }

    @Unique
    private void applyTransformation(ModelPartTransformation t) {
        ModelPart targetPart = getModelPart(t.getModelPart());
        if (targetPart == null) return;

        String partName = t.getModelPart().toLowerCase();
        String type = t.getType().toLowerCase();
        float value = t.getValue();
        boolean overrideAnimation = t.getOverrideAnimation();

        switch (type) {
            case "pitch" -> {
                if (overrideAnimation) {
                    targetPart.pitch = value;
                    SYNC$LOCKED_VALUES.put(partName + "_pitch", value);
                } else {
                    targetPart.pitch += value;
                }
            }
            case "yaw" -> {
                if (overrideAnimation) {
                    targetPart.yaw = value;
                    SYNC$LOCKED_VALUES.put(partName + "_yaw", value);
                } else {
                    targetPart.yaw += value;
                }
            }
            case "roll" -> {
                if (overrideAnimation) {
                    targetPart.roll = value;
                    SYNC$LOCKED_VALUES.put(partName + "_roll", value);
                } else {
                    targetPart.roll += value;
                }
            }
            case "visible" -> targetPart.visible = value != 0;
            case "hidden" -> targetPart.hidden = value != 0;
            case "xscale" -> {
                float originalX = SYNC$ORIGINAL_VALUES.getOrDefault(partName + "_xScale", 1.0f);
                targetPart.xScale = originalX + value;
            }
            case "yscale" -> {
                float originalY = SYNC$ORIGINAL_VALUES.getOrDefault(partName + "_yScale", 1.0f);
                targetPart.yScale = originalY + value;
            }
            case "zscale" -> {
                float originalZ = SYNC$ORIGINAL_VALUES.getOrDefault(partName + "_zScale", 1.0f);
                targetPart.zScale = originalZ + value;
            }
            case "pivotx" -> targetPart.pivotX += value;
            case "pivoty" -> targetPart.pivotY += value;
            case "pivotz" -> targetPart.pivotZ += value;
        }
    }
}
