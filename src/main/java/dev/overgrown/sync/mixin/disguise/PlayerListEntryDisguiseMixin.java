package dev.overgrown.sync.mixin.disguise;

import com.mojang.authlib.GameProfile;
import dev.overgrown.sync.data.disguise.DisguiseData;
import dev.overgrown.sync.data.disguise.client.ClientDisguiseManager;
import dev.overgrown.sync.data.disguise.client.OfflinePlayerSkinCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a player is disguised as another player, redirects
 * {@link PlayerListEntry#getSkinTextures()} to the target player's textures.
 * Falls back to {@link OfflinePlayerSkinCache} for offline players.
 */
@Environment(EnvType.CLIENT)
@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryDisguiseMixin {

    @Shadow @Final private GameProfile profile;

    @Unique
    private static final ThreadLocal<Boolean> sync$resolving = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    public void sync$getDisguisedSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        if (sync$resolving.get()) return;

        sync$resolving.set(true);
        try {
            PlayerListEntry targetEntry = sync$findTargetEntry();
            if (targetEntry != null) {
                cir.setReturnValue(targetEntry.getSkinTextures());
                return;
            }

            SkinTextures offline = sync$buildOfflineSkin();
            if (offline != null) cir.setReturnValue(offline);
        } finally {
            sync$resolving.set(false);
        }
    }

    @Unique
    @Nullable
    private PlayerListEntry sync$findTargetEntry() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.getNetworkHandler() == null) return null;

        PlayerEntity player = client.world.getPlayerByUuid(this.profile.getId());
        if (player == null) return null;

        DisguiseData disguise = ClientDisguiseManager.getDisguise(player.getId());
        if (disguise == null || !disguise.isPlayerDisguise()) return null;

        if (disguise.getTargetPlayerUuid().equals(this.profile.getId())) return null;

        return client.getNetworkHandler().getPlayerListEntry(disguise.getTargetPlayerUuid());
    }

    @Unique
    @Nullable
    private SkinTextures sync$buildOfflineSkin() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.getNetworkHandler() == null) return null;

        PlayerEntity player = client.world.getPlayerByUuid(this.profile.getId());
        if (player == null) return null;

        DisguiseData disguise = ClientDisguiseManager.getDisguise(player.getId());
        if (disguise == null || !disguise.isPlayerDisguise()) return null;

        Identifier offlineSkin = OfflinePlayerSkinCache.getSkin(disguise.getTargetPlayerUuid());
        if (offlineSkin == null) return null;

        SkinTextures.Model model = OfflinePlayerSkinCache.getModel(disguise.getTargetPlayerUuid());
        if (model == null) model = SkinTextures.Model.WIDE;

        SkinTextures fallback = DefaultSkinHelper.getSkinTextures(disguise.getTargetPlayerUuid());
        return new SkinTextures(offlineSkin, null, fallback.capeTexture(), fallback.elytraTexture(), model, true);
    }
}
