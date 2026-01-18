package dev.overgrown.sync.mixin;

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

    @Shadow
    @Final
    private GameProfile profile;

    @Shadow
    public abstract String getModel();

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    public void getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        var mc = MinecraftClient.getInstance();

        if (mc.world != null) {
            var player = mc.world.getPlayerByUuid(this.profile.getId());
            boolean isFirstPerson = mc.player == player && mc.options.getPerspective() == Perspective.FIRST_PERSON;
            var texture = RenderingUtils.getPrimaryOverlayTexture(player,
                    this.getModel().equalsIgnoreCase("slim"), isFirstPerson);

            if (texture != null) {
                cir.setReturnValue(texture);
            }
        }
    }

}
