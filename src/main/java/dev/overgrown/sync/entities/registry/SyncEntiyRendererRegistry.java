package dev.overgrown.sync.entities.registry;

import dev.overgrown.sync.entities.clone.renderer.CloneEntityRenderer;
import dev.overgrown.sync.entities.custom_projectile.renderer.CustomProjectileRenderer;
import dev.overgrown.sync.entities.minion.renderer.MinionEntityRenderer;
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