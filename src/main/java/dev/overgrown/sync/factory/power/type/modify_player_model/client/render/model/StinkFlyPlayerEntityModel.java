package dev.overgrown.sync.factory.power.type.modify_player_model.client.render.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

public class StinkFlyPlayerEntityModel<T extends LivingEntity> extends PlayerEntityModel<T> {

    private final ModelPart right_second_leg;
    private final ModelPart right_second_pants;
    private final ModelPart left_second_leg;
    private final ModelPart left_second_pants;

    public StinkFlyPlayerEntityModel(ModelPart root, boolean thinArms) {
        super(root, thinArms);

        this.right_second_leg = root.getChild("right_second_leg");
        this.right_second_pants = root.getChild("right_second_pants");
        this.left_second_leg = root.getChild("left_second_leg");
        this.left_second_pants = root.getChild("left_second_pants");
    }

    public static TexturedModelData getTexturedModelData() {
        return TexturedModelData.of(getTexturedModelData(Dilation.NONE, false), 64, 64);
    }

    public static TexturedModelData getSlimTexturedModelData() {
        return TexturedModelData.of(getTexturedModelData(Dilation.NONE, true), 64, 64);
    }

    public static ModelData getTexturedModelData(Dilation dilation, boolean slim) {
        ModelData modelData = PlayerEntityModel.getTexturedModelData(dilation, slim);
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 5.0F, -2.0F));
        ModelPartData hat = modelPartData.addChild("hat", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)), ModelTransform.pivot(0.0F, 5.0F, -2.0F));

        ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.0F, 5.5F));
        ModelPartData lower_r1 = body.addChild("lower_r1", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 1.0F, 4.5F, -1.0036F, 0.0F, 0.0F));
        ModelPartData upper_r1 = body.addChild("upper_r1", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -1.0F, -4.5F, 0.4363F, 0.0F, 0.0F));

        ModelPartData jacket = modelPartData.addChild("jacket", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.0F, 5.5F));
        ModelPartData lower_r2 = jacket.addChild("lower_r2", ModelPartBuilder.create().uv(16, 32).cuboid(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation).mirrored(false), ModelTransform.of(0.0F, 1.0F, 4.5F, -1.0036F, 0.0F, 0.0F));
        ModelPartData upper_r2 = jacket.addChild("upper_r2", ModelPartBuilder.create().uv(16, 32).cuboid(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), ModelTransform.of(0.0F, -1.0F, -4.5F, 0.4363F, 0.0F, 0.0F));

        ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-4.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-4.0F, 12.0F, 3.0F));
        ModelPartData right_pants = modelPartData.addChild("right_pants", ModelPartBuilder.create().uv(0, 32).cuboid(-4.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(-4.0F, 12.0F, 3.0F));

        ModelPartData right_second_leg = modelPartData.addChild("right_second_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F));
        ModelPartData right_second_pants = modelPartData.addChild("right_second_pants", ModelPartBuilder.create().uv(0, 32).cuboid(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.of(-4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(16, 48).cuboid(0.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, 12.0F, 3.0F));
        ModelPartData left_pants = modelPartData.addChild("left_pants", ModelPartBuilder.create().uv(0, 48).cuboid(0.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(4.0F, 12.0F, 3.0F));

        ModelPartData left_second_leg = modelPartData.addChild("left_second_leg", ModelPartBuilder.create().uv(16, 48).cuboid(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F));
        ModelPartData left_second_pants = modelPartData.addChild("left_second_pants", ModelPartBuilder.create().uv(0, 48).cuboid(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.of(4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        if (slim) {
            ModelPartData left_arm = modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(0.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, 6.0F, -1.5F, -0.5236F, 0.0F, 0.0F));
            ModelPartData left_sleeve = modelPartData.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(0.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.of(4.0F, 6.0F, -1.5F, -0.5236F, 0.0F, 0.0F));
            ModelPartData right_arm = modelPartData.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-4.0F, 6.0F, -2.0F, -0.5236F, 0.0F, 0.0F));
            ModelPartData right_sleeve = modelPartData.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-3.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.of(-4.0F, 6.0F, -2.0F, -0.5236F, 0.0F, 0.0F));
        } else {
            ModelPartData left_arm = modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, 6.0F, -1.5F, -0.5236F, 0.0F, 0.0F));
            ModelPartData left_sleeve = modelPartData.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.of(4.0F, 6.0F, -1.5F, -0.5236F, 0.0F, 0.0F));
            ModelPartData right_arm = modelPartData.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, 6.0F, -2.0F, -0.5236F, 0.0F, 0.0F));
            ModelPartData right_sleeve = modelPartData.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.of(-4.0F, 6.0F, -2.0F, -0.5236F, 0.0F, 0.0F));
        }

        return modelData;
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.right_second_leg, this.right_second_pants, this.left_second_leg, this.left_second_pants));
    }

    @Override
    public void setAngles(T livingEntity, float f, float g, float h, float i, float j) {
        for (ModelPart bodyPart : this.getBodyParts()) {
            bodyPart.resetTransform();
        }

        for (ModelPart bodyPart : this.getHeadParts()) {
            bodyPart.resetTransform();
        }

        super.setAngles(livingEntity, f, g, h, i, j);

        this.copyRotations(this.rightLeg, this.right_second_leg);
        this.copyRotations(this.leftLeg, this.left_second_leg);
        this.copyRotations(this.rightPants, this.right_second_pants);
        this.copyRotations(this.leftPants, this.left_second_pants);

        this.right_second_pants.visible = this.rightPants.visible;
        this.left_second_pants.visible = this.leftPants.visible;

        this.body.pivotY += this.sneaking ? 9 : 12;
        this.jacket.pivotY += this.sneaking ? 9 : 12;

        this.head.pivotY += 5;
        this.hat.pivotY += 5;

        this.rightLeg.pivotZ += 3;
        this.rightLeg.pivotY -= this.sneaking ? 2 : 0;
        this.rightPants.pivotZ += 3;
        this.rightPants.pivotY -= this.sneaking ? 2 : 0;
        this.right_second_leg.pivotZ += this.sneaking ? 4 : 0;
        this.right_second_pants.pivotZ += this.sneaking ? 4 : 0;

        this.leftLeg.pivotZ += 3;
        this.leftLeg.pivotY -= this.sneaking ? 2 : 0;
        this.leftPants.pivotZ += 3;
        this.leftPants.pivotY -= this.sneaking ? 2 : 0;
        this.left_second_leg.pivotZ += this.sneaking ? 4 : 0;
        this.left_second_pants.pivotZ += this.sneaking ? 4 : 0;

        this.rightArm.pivotY += 4;
        this.rightArm.pivotX += 1;
        this.rightSleeve.pivotY += 4;
        this.rightSleeve.pivotX += 1;
        this.leftArm.pivotY += 4;
        this.leftArm.pivotX -= 1;
        this.leftSleeve.pivotY += 4;
        this.leftSleeve.pivotX -= 1;

        for (ModelPart bodyPart : this.getBodyParts()) {
            var defaultPose = bodyPart.getDefaultTransform();
            bodyPart.yaw += defaultPose.yaw;
            bodyPart.pitch += defaultPose.pitch;
            bodyPart.roll += defaultPose.roll;
        }
    }

    private void copyRotations(ModelPart from, ModelPart to) {
        to.yaw = from.yaw;
        to.pitch = from.pitch;
        to.roll = from.roll;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.right_second_leg.visible = visible;
        this.right_second_pants.visible = visible;
        this.left_second_leg.visible = visible;
        this.left_second_pants.visible = visible;
    }
}