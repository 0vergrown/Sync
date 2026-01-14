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

    public static Identifier getPrimaryOverlayTexture(LivingEntity entity) {
        List<EntityTextureOverlayPower> powers = getTextureOverlays(entity);
        if (!powers.isEmpty() && powers.get(0).isActive()) {
            return powers.get(0).getTextureLocation();
        }
        return null;
    }
}