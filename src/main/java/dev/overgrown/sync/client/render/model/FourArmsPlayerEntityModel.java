package dev.overgrown.sync.client.render.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

public class FourArmsPlayerEntityModel<T extends LivingEntity> extends PlayerEntityModel<T> {

    public final ModelPart rightSecondArm;
    public final ModelPart rightSecondSleeve;
    public final ModelPart leftSecondArm;
    public final ModelPart leftSecondSleeve;

    public FourArmsPlayerEntityModel(ModelPart root, boolean thinArms) {
        super(root, thinArms);

        this.rightSecondArm = root.getChild("right_second_arm");
        this.rightSecondSleeve = root.getChild("right_second_sleeve");
        this.leftSecondArm = root.getChild("left_second_arm");
        this.leftSecondSleeve = root.getChild("left_second_sleeve");
    }

    public static TexturedModelData getTexturedModelData () {
        return TexturedModelData.of(getTexturedModelData(Dilation.NONE, false), 64, 64);
    }

    public static TexturedModelData getSlimTexturedModelData () {
        return TexturedModelData.of(getTexturedModelData(Dilation.NONE, true), 64, 64);
    }

    public static ModelData getTexturedModelData(Dilation dilation, boolean slim) {
        ModelData modelData = PlayerEntityModel.getTexturedModelData(dilation, slim);
        ModelPartData modelPartData = modelData.getRoot();

        if (slim) {
            modelPartData.addChild("left_second_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(5.0F, 5.5F, 0.0F));
            modelPartData.addChild("right_second_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-5.0F, 5.5F, 0.0F));
            modelPartData.addChild("left_second_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation.add(0.25F)), ModelTransform.pivot(5.0F, 5.5F, 0.0F));
            modelPartData.addChild("right_second_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, dilation.add(0.25F)), ModelTransform.pivot(-5.0F, 5.5F, 0.0F));
        } else {
            modelPartData.addChild("left_second_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(5.0F, 5.0F, 0.0F));
            modelPartData.addChild("right_second_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-5.0F, 5.0F, 0.0F));
            modelPartData.addChild("left_second_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)), ModelTransform.pivot(5.0F, 5.0F, 0.0F));
            modelPartData.addChild("right_second_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)), ModelTransform.pivot(-5.0F, 5.0F, 0.0F));
        }

        return modelData;
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.rightSecondArm, this.leftSecondArm, this.rightSecondSleeve, this.leftSecondSleeve));
    }

    @Override
    public void setAngles(T livingEntity, float f, float g, float h, float i, float j) {
        for (ModelPart bodyPart : this.getBodyParts()) {
            bodyPart.resetTransform();
        }

        super.setAngles(livingEntity, f, g, h, i, j);

        this.rightArm.pitch -= (float) Math.toRadians(17);
        this.rightSleeve.pitch -= (float) Math.toRadians(17);
        this.rightArm.roll += (float) Math.toRadians(27);
        this.rightSleeve.roll += (float) Math.toRadians(27);
        this.leftArm.pitch -= (float) Math.toRadians(17);
        this.leftSleeve.pitch -= (float) Math.toRadians(17);
        this.leftArm.roll -= (float) Math.toRadians(27);
        this.leftSleeve.roll -= (float) Math.toRadians(27);

        this.rightSecondArm.copyTransform(this.rightArm);
        this.leftSecondArm.copyTransform(this.leftArm);
        this.rightSecondSleeve.copyTransform(this.rightSleeve);
        this.leftSecondSleeve.copyTransform(this.leftSleeve);

        this.rightSecondArm.roll -= (float) Math.toRadians(17);
        this.rightSecondSleeve.roll -= (float) Math.toRadians(17);
        this.leftSecondArm.roll += (float) Math.toRadians(17);
        this.leftSecondSleeve.roll += (float) Math.toRadians(17);

        this.rightSecondArm.pivotY += 3;
        this.leftSecondArm.pivotY += 3;
        this.rightSecondSleeve.pivotY += 3;
        this.leftSecondSleeve.pivotY += 3;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.rightSecondArm.visible = visible;
        this.leftSecondArm.visible = visible;
        this.rightSecondSleeve.visible = visible;
        this.leftSecondSleeve.visible = visible;
    }
}
