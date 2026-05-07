package dev.overgrown.sync.registry;

import dev.overgrown.sync.power.type.custom_projectile.entities.renderer.CustomProjectileRenderer;
import dev.overgrown.sync.power.type.summons.entities.clone.renderer.CloneEntityRenderer;
import dev.overgrown.sync.power.type.summons.entities.minion.renderer.MinionEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class SyncEntityRendererRegistry {

    public static void register() {
        EntityRendererRegistry.register(SyncEntityRegistry.CUSTOM_PROJECTILE, CustomProjectileRenderer::new);
        EntityRendererRegistry.register(SyncEntityRegistry.MINION, MinionEntityRenderer::new);
        EntityRendererRegistry.register(SyncEntityRegistry.CLONE, ctx -> new CloneEntityRenderer<>(ctx, false));

        SyncEntityModelLayerRegistry.register();
    }
}
