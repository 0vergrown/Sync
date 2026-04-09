package dev.overgrown.sync.mixin.summon_clone;

import java.util.Map;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.renderer.CloneEntityRenderer;
import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import dev.overgrown.sync.mixin.summon_clone.accessor.LivingEntityRendererAccessor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Shadow
    private @Final ItemRenderer itemRenderer;

    @Shadow
    private @Final TextRenderer textRenderer;

    @Shadow
    private @Final EntityModelLoader modelLoader;

    @Shadow
    private @Final HeldItemRenderer heldItemRenderer;

    @Shadow
    private @Final BlockRenderManager blockRenderManager;

    @Unique
    private Map<String, EntityRenderer<CloneEntity>> cloneRenderers = ImmutableMap.of();

    @SuppressWarnings("unchecked")
    @Inject(
            at=@At(
                    "HEAD"
            ),
            method="getRenderer",
            cancellable=true
    )
    public <T extends Entity> void getCloneRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (entity instanceof CloneEntity clone) {
            if (clone.isOwned()) {
                try {
                    // Safe navigation with null checks
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.getNetworkHandler() == null) {
                        cir.setReturnValue((EntityRenderer<? super T>)cloneRenderers.get("default"));
                        return;
                    }

                    PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(clone.getOwnerUuid());
                    if (playerListEntry == null || playerListEntry.getModel() == null) {
                        cir.setReturnValue((EntityRenderer<? super T>)cloneRenderers.get("default"));
                        return;
                    }

                    String modelType = playerListEntry.getModel();
                    EntityRenderer<? super T> renderer = (EntityRenderer<? super T>) cloneRenderers.get(modelType);
                    // Fallback to default if specific renderer not found
                    cir.setReturnValue(renderer != null ? renderer : (EntityRenderer<? super T>)cloneRenderers.get("default"));
                } catch (NullPointerException e) {
                    Sync.LOGGER.warn("Failed to get clone renderer, using default", e);
                    cir.setReturnValue((EntityRenderer<? super T>)cloneRenderers.get("default"));
                }
            } else {
                cir.setReturnValue((EntityRenderer<? super T>)cloneRenderers.get("default"));
            }
        }
    }

    @Inject(
            at = @At(
                    "HEAD"
            ),
            method="reload"
    )
    public void reload (ResourceManager manager, CallbackInfo info) {
        Context context = new Context((EntityRenderDispatcher)(Object)this, this.itemRenderer, this.blockRenderManager, this.heldItemRenderer, manager, this.modelLoader, this.textRenderer);
        this.cloneRenderers = ImmutableMap.of(
                "default", createCloneEntityRenderer(context, false),
                "slim", createCloneEntityRenderer(context, true)
        );
    }

    @Unique
    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    private CloneEntityRenderer<CloneEntity> createCloneEntityRenderer (Context context, boolean slimArms) {
        try {
            CloneEntityRenderer<CloneEntity> renderer = new CloneEntityRenderer<>(context, slimArms);
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)renderer;
            LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker()
                    .registerRenderers(SyncEntityRegistry.CLONE, renderer, new RegistrationHelperImpl(accessor::invokeAddFeature), context);
            return renderer;
        } catch (Exception e) {
            Sync.LOGGER.error("Failed to create CloneEntityRenderer: {}", e.getMessage());
            // Return a fallback renderer to prevent crash
            return new CloneEntityRenderer<CloneEntity>(context, false) {
                @Override
                public Identifier getTexture(CloneEntity clone) {
                    return new Identifier("minecraft", "textures/entity/steve.png");
                }
            };
        }
    }
}