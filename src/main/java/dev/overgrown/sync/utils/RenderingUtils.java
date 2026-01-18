package dev.overgrown.sync.utils;

import dev.overgrown.sync.factory.power.type.entity_texture_overlay.EntityTextureOverlayPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class RenderingUtils {

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
}