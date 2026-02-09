package dev.overgrown.sync.registry.entities;

import dev.overgrown.sync.factory.action.entity.summons.entities.clone.renderer.CloneEntityRenderer;
import dev.overgrown.sync.factory.power.type.custom_projectile.entities.renderer.CustomProjectileRenderer;
import dev.overgrown.sync.factory.action.entity.summons.entities.minion.renderer.MinionEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class SyncEntiyRendererRegistry {
    public static void register() {
        EntityRendererRegistry.register(SyncEntityRegistry.CLONE, (context) ->
                new CloneEntityRenderer<>(context, false));
        EntityRendererRegistry.register(SyncEntityRegistry.MINION, MinionEntityRenderer::new);
        EntityRendererRegistry.register(SyncEntityRegistry.CUSTOM_PROJECTILE, CustomProjectileRenderer::new);
    }
}