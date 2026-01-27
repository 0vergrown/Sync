package dev.overgrown.sync.mixin.entity_texture_overlay;

import com.mojang.authlib.GameProfile;
import dev.overgrown.sync.utils.RenderingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow @Final private GameProfile profile;
    @Shadow public abstract String getModel();
    @Shadow protected abstract void loadTextures();

    @Inject(
            method = "getSkinTexture",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    public void getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        var mc = MinecraftClient.getInstance();

        if (mc.world != null) {
            var player = mc.world.getPlayerByUuid(this.profile.getId());
            if (player != null) {
                this.loadTextures();

                boolean isFirstPerson = mc.player == player && mc.options.getPerspective() == Perspective.FIRST_PERSON;
                var texture = RenderingUtils.getPrimaryOverlayTexture(player,
                        this.getModel().equalsIgnoreCase("slim"), isFirstPerson);

                if (texture != null) {
                    var powers = RenderingUtils.getTextureOverlays(player);
                    if (!powers.isEmpty()) {
                        var power = powers.get(0);

                        // For overlay mode, keep the original skin texture and render the overlay on top separately
                        if (power.shouldRenderAsOverlay()) {
                            // Don't replace the texture - keep the original skin
                            // The overlay will be rendered on top by the feature renderer or arm overlay
                            return;
                        }

                        // For replace mode, replace the texture if appropriate
                        if (!isFirstPerson || power.shouldShowFirstPerson()) {
                            cir.setReturnValue(texture);
                        }
                    }
                }
            }
        }
    }
}