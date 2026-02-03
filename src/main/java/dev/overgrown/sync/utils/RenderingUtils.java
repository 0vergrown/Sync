package dev.overgrown.sync.utils;

import dev.overgrown.sync.client.render.model.FourArmsPlayerEntityModel;
import dev.overgrown.sync.client.render.model.StinkFlyPlayerEntityModel;
import dev.overgrown.sync.entities.registry.SyncEntityModelLayerRegistry;
import dev.overgrown.sync.factory.power.type.EntityTextureOverlayPower;
import dev.overgrown.sync.factory.power.type.ModifyPlayerModelPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class RenderingUtils {

    private static FourArmsPlayerEntityModel<AbstractClientPlayerEntity> FOUR_ARMS_MODEL;
    private static FourArmsPlayerEntityModel<AbstractClientPlayerEntity> FOUR_ARMS_MODEL_SLIM;
    private static StinkFlyPlayerEntityModel<AbstractClientPlayerEntity> STINKFLY_MODEL;
    private static StinkFlyPlayerEntityModel<AbstractClientPlayerEntity> STINKFLY_MODEL_SLIM;

    public static boolean hasTextureOverlay(LivingEntity entity) {
        return !PowerHolderComponent.getPowers(entity, EntityTextureOverlayPower.class).isEmpty();
    }

    public static List<EntityTextureOverlayPower> getTextureOverlays(LivingEntity entity) {
        return PowerHolderComponent.getPowers(entity, EntityTextureOverlayPower.class);
    }

    public static Identifier getPrimaryOverlayTexture(LivingEntity entity, boolean slimModel, boolean firstPerson) {
        List<EntityTextureOverlayPower> powers = getTextureOverlays(entity);
        if (!powers.isEmpty() && powers.get(0).isActive() && (powers.get(0).shouldShowFirstPerson() || !firstPerson)) {
            return slimModel ? powers.get(0).getSlimTextureLocation() : powers.get(0).getWideTextureLocation();
        }
        return null;
    }

    // Check if cape should be hidden
    public static boolean shouldHideCape(LivingEntity entity) {
        List<EntityTextureOverlayPower> powers = getTextureOverlays(entity);
        return !powers.isEmpty() && powers.get(0).isActive() && powers.get(0).shouldHideCape();
    }

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