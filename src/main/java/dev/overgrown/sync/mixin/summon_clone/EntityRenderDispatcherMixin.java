package dev.overgrown.sync.mixin.summon_clone;

import com.google.common.collect.ImmutableMap;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.mixin.summon_clone.accessor.LivingEntityRendererAccessor;
import dev.overgrown.sync.power.type.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.power.type.summons.entities.clone.renderer.CloneEntityRenderer;
import dev.overgrown.sync.registry.SyncEntityRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow @Final private TextRenderer textRenderer;
    @Shadow @Final private EntityModelLoader modelLoader;
    @Shadow @Final private HeldItemRenderer heldItemRenderer;
    @Shadow @Final private BlockRenderManager blockRenderManager;

    @Unique
    private Map<SkinTextures.Model, EntityRenderer<CloneEntity>> sync$cloneRenderers = ImmutableMap.of();

    @SuppressWarnings("unchecked")
    @Inject(at = @At("HEAD"), method = "getRenderer", cancellable = true)
    public <T extends Entity> void sync$getCloneRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (!(entity instanceof CloneEntity clone)) return;

        EntityRenderer<CloneEntity> wide = sync$cloneRenderers.get(SkinTextures.Model.WIDE);
        if (wide == null) return;

        if (!clone.isOwned()) {
            cir.setReturnValue((EntityRenderer<? super T>) wide);
            return;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() == null) {
                cir.setReturnValue((EntityRenderer<? super T>) wide);
                return;
            }

            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(clone.getOwnerUuid());
            SkinTextures.Model model = entry != null ? entry.getSkinTextures().model() : SkinTextures.Model.WIDE;
            EntityRenderer<CloneEntity> renderer = sync$cloneRenderers.getOrDefault(model, wide);
            cir.setReturnValue((EntityRenderer<? super T>) renderer);
        } catch (Exception e) {
            Sync.LOGGER.warn("Failed to get clone renderer, using default", e);
            cir.setReturnValue((EntityRenderer<? super T>) wide);
        }
    }

    @Inject(at = @At("HEAD"), method = "reload")
    public void sync$reload(ResourceManager manager, CallbackInfo info) {
        Context context = new Context(
            (EntityRenderDispatcher) (Object) this, this.itemRenderer, this.blockRenderManager,
            this.heldItemRenderer, manager, this.modelLoader, this.textRenderer
        );
        this.sync$cloneRenderers = ImmutableMap.of(
            SkinTextures.Model.WIDE, sync$createCloneEntityRenderer(context, false),
            SkinTextures.Model.SLIM, sync$createCloneEntityRenderer(context, true)
        );
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CloneEntityRenderer<CloneEntity> sync$createCloneEntityRenderer(Context context, boolean slimArms) {
        try {
            CloneEntityRenderer<CloneEntity> renderer = new CloneEntityRenderer<>(context, slimArms);
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) renderer;
            LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker()
                .registerRenderers(SyncEntityRegistry.CLONE, renderer,
                    new RegistrationHelperImpl(accessor::invokeAddFeature), context);
            return renderer;
        } catch (Exception e) {
            Sync.LOGGER.error("Failed to create CloneEntityRenderer: {}", e.getMessage());
            return new CloneEntityRenderer<>(context, false) {
                @Override
                public Identifier getTexture(CloneEntity clone) {
                    return Identifier.ofVanilla("textures/entity/player/wide/steve.png");
                }
            };
        }
    }
}
