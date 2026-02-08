package dev.overgrown.sync.utils.modify_player_model;

import dev.overgrown.sync.client.render.model.FourArmsPlayerEntityModel;
import dev.overgrown.sync.client.render.model.StinkFlyPlayerEntityModel;
import dev.overgrown.sync.entities.registry.SyncEntityModelLayerRegistry;
import dev.overgrown.sync.factory.power.type.ModifyPlayerModelPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class ModelRendering {

    private static FourArmsPlayerEntityModel<AbstractClientPlayerEntity> FOUR_ARMS_MODEL;
    private static FourArmsPlayerEntityModel<AbstractClientPlayerEntity> FOUR_ARMS_MODEL_SLIM;
    private static StinkFlyPlayerEntityModel<AbstractClientPlayerEntity> STINKFLY_MODEL;
    private static StinkFlyPlayerEntityModel<AbstractClientPlayerEntity> STINKFLY_MODEL_SLIM;


    public static boolean hasSmallArms(AbstractClientPlayerEntity player) {
        return player.getModel().equalsIgnoreCase("slim");
    }

    public static void bakeModels(EntityRendererFactory.Context ctx, boolean slim) {
        if (slim) {
            FOUR_ARMS_MODEL_SLIM = new FourArmsPlayerEntityModel<>(ctx.getPart(SyncEntityModelLayerRegistry.FOUR_ARMS_PLAYER_SLIM_MODEL_LAYER), true);
            STINKFLY_MODEL_SLIM = new StinkFlyPlayerEntityModel<>(ctx.getPart(SyncEntityModelLayerRegistry.STINKFLY_PLAYER_SLIM_MODEL_LAYER), true);
        } else {
            FOUR_ARMS_MODEL = new FourArmsPlayerEntityModel<>(ctx.getPart(SyncEntityModelLayerRegistry.FOUR_ARMS_PLAYER_MODEL_LAYER), false);
            STINKFLY_MODEL = new StinkFlyPlayerEntityModel<>(ctx.getPart(SyncEntityModelLayerRegistry.STINKFLY_PLAYER_MODEL_LAYER), false);
        }
    }

    public static PlayerEntityModel<AbstractClientPlayerEntity> getOverriddenPlayerModel(AbstractClientPlayerEntity player, PlayerEntityModel<AbstractClientPlayerEntity> original) {
        for (ModifyPlayerModelPower power : PowerHolderComponent.getPowers(player, ModifyPlayerModelPower.class)) {
            if (power.model == ModifyPlayerModelPower.Model.FOUR_ARMS) {
                return hasSmallArms(player) ? FOUR_ARMS_MODEL_SLIM : FOUR_ARMS_MODEL;
            } else if (power.model == ModifyPlayerModelPower.Model.STINKFLY) {
                return hasSmallArms(player) ? STINKFLY_MODEL_SLIM : STINKFLY_MODEL;
            }
        }

        return original;
    }
}
