package dev.overgrown.sync.client.render.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ExtraLimbsEntityModel<T extends LivingEntity> extends BipedEntityModel<T> {

    public final ModelPart rightSecondArm;
    public final ModelPart leftSecondArm;

    public ExtraLimbsEntityModel(ModelPart root) {
        super(root);
    }

    public ExtraLimbsEntityModel(ModelPart root, Function<Identifier, RenderLayer> renderLayerFactory) {
        super(root, renderLayerFactory);
    }
}
