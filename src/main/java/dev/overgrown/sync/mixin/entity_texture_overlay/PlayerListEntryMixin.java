package dev.overgrown.sync.mixin.entity_texture_overlay;

import com.mojang.authlib.GameProfile;
import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow @Final private GameProfile profile;

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    public void sync$getSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        SkinTextures original = cir.getReturnValue();
        if (original == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        PlayerEntity player = mc.world.getPlayerByUuid(this.profile.getId());
        if (player == null) return;

        List<EntityTextureOverlayPowerType> powers =
            PowerHolderComponent.getPowerTypes(player, EntityTextureOverlayPowerType.class);
        if (powers.isEmpty()) return;

        EntityTextureOverlayPowerType power = powers.get(0);
        if (!power.isActive()) return;

        // Overlay mode: keep original skin; the feature renderer draws on top.
        if (power.shouldRenderAsOverlay()) return;

        // Replace mode: swap the texture to the overlay's chosen texture.
        boolean isFirstPerson = mc.player == player && mc.options.getPerspective() == Perspective.FIRST_PERSON;
        if (isFirstPerson && !power.shouldShowFirstPerson()) return;

        boolean slim = original.model() == SkinTextures.Model.SLIM;
        Identifier replacement = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();
        if (replacement == null) return;

        cir.setReturnValue(new SkinTextures(
            replacement,
            original.textureUrl(),
            original.capeTexture(),
            original.elytraTexture(),
            original.model(),
            original.secure()
        ));
    }
}
