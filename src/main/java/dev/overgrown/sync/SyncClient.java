package dev.overgrown.sync;

import dev.overgrown.sync.action.type.entity.radial_menu.client.RadialMenuClient;
import dev.overgrown.sync.condition.type.entity.key_pressed.client.KeyStateTracker;
import dev.overgrown.sync.data.disguise.client.DisguiseClientInit;
import dev.overgrown.sync.data.keybind.client.KeybindClientInit;
import dev.overgrown.sync.data.rope.client.RopeClientInit;
import dev.overgrown.sync.power.type.action_on_sending_message.util.TranslationKeyResolver;
import dev.overgrown.sync.registry.SyncEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

@Environment(EnvType.CLIENT)
public class SyncClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyStateTracker.register();
        DisguiseClientInit.init();
        RopeClientInit.init();
        KeybindClientInit.init();
        SyncEntityRendererRegistry.register();
        RadialMenuClient.register();

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new IdentifiableResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Sync.identifier("translation_key_resolver");
                }

                @Override
                public Collection<Identifier> getFabricDependencies() {
                    return Collections.emptyList();
                }

                @Override
                public java.util.concurrent.CompletableFuture<Void> reload(
                    Synchronizer synchronizer, ResourceManager manager,
                    net.minecraft.util.profiler.Profiler prepareProfiler,
                    net.minecraft.util.profiler.Profiler applyProfiler,
                    java.util.concurrent.Executor prepareExecutor,
                    java.util.concurrent.Executor applyExecutor) {
                    return java.util.concurrent.CompletableFuture
                        .runAsync(() -> TranslationKeyResolver.loadFromResourceManager(manager), prepareExecutor)
                        .thenCompose(synchronizer::whenPrepared);
                }
            }
        );
    }
}
