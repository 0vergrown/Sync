package dev.overgrown.sync.entities.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.entities.clone.model.CloneEntityModel;
import dev.overgrown.sync.entities.minion.model.MinionEntityModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class SyncEntityModelLayerRegistry {
    public static final Dilation ARMOR_DILATION = new Dilation(1.0f);
    public static final Dilation HAT_DILATION = new Dilation(0.5f);

    public static final EntityModelLayer CLONE_MODEL_LAYER = new EntityModelLayer(Sync.identifier("player_clone"), "main");
    public static final EntityModelLayer CLONE_INNER_LAYER = new EntityModelLayer(Sync.identifier("player_clone"), "inner_armour");
    public static final EntityModelLayer CLONE_OUTER_LAYER = new EntityModelLayer(Sync.identifier("player_clone"), "outer_armour");

    public static final EntityModelLayer CLONE_SLIM_MODEL_LAYER = new EntityModelLayer(Sync.identifier("player_clone_slim"), "main");
    public static final EntityModelLayer CLONE_SLIM_INNER_LAYER = new EntityModelLayer(Sync.identifier("player_clone_slim"), "inner_armour");
    public static final EntityModelLayer CLONE_SLIM_OUTER_LAYER = new EntityModelLayer(Sync.identifier("player_clone_slim"), "outer_armour");

    public static final EntityModelLayer MINION_MODEL_LAYER = new EntityModelLayer(Sync.identifier("minion"), "main");

    public static void register () {
        EntityModelLayerRegistry.registerModelLayer(CLONE_MODEL_LAYER, CloneEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(CLONE_INNER_LAYER, CloneEntityModel::getTexturedModelDataInner);
        EntityModelLayerRegistry.registerModelLayer(CLONE_OUTER_LAYER, CloneEntityModel::getTexturedModelDataOuter);

        EntityModelLayerRegistry.registerModelLayer(CLONE_SLIM_MODEL_LAYER, CloneEntityModel::getSlimTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(CLONE_SLIM_INNER_LAYER, CloneEntityModel::getTexturedModelDataInner);
        EntityModelLayerRegistry.registerModelLayer(CLONE_SLIM_OUTER_LAYER, CloneEntityModel::getTexturedModelDataOuter);

        EntityModelLayerRegistry.registerModelLayer(MINION_MODEL_LAYER, MinionEntityModel::getTexturedModelData);
    }
}
