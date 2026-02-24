package dev.overgrown.sync.mixin.disguise;

import com.mojang.authlib.GameProfile;
import dev.overgrown.sync.factory.disguise.DisguiseData;
import dev.overgrown.sync.factory.disguise.client.ClientDisguiseManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
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
 * When a player is disguised as another player, this mixin redirects
 * {@link PlayerListEntry#getSkinTexture()} and {@link PlayerListEntry#getModel()}
 * so that the disguised player renders with the target player's skin and arm style.
 */
@Environment(EnvType.CLIENT)
@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryDisguiseMixin {

    @Shadow @Final private GameProfile profile;

    @Shadow
    public abstract String getModel();

    @Shadow
    protected abstract void loadTextures();

    // Skin texture
    @Inject(
            method = "getSkinTexture",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    public void sync$getDisguisedSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        PlayerListEntry targetEntry = findTargetEntry();
        if (targetEntry != null) {
            cir.setReturnValue(targetEntry.getSkinTexture());
        }
    }

    // Arm model (slim vs. wide)
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    public void sync$getDisguisedModel(CallbackInfoReturnable<String> cir) {
        PlayerListEntry targetEntry = findTargetEntry();
        if (targetEntry != null) {
            cir.setReturnValue(targetEntry.getModel());
        }
    }

    // Helpers
    /**
     * Looks up the {@link PlayerListEntry} for the player that the owner of
     * <em>this</em> entry is currently disguised as, or {@code null} if no
     * player-disguise is active for them.
     */
    @Unique
    @Nullable
    private PlayerListEntry findTargetEntry() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.getNetworkHandler() == null) return null;

        // Find the in-world entity whose profile matches this entry.
        PlayerEntity player = client.world.getPlayerByUuid(this.profile.getId());
        if (player == null) return null;

        DisguiseData disguise = ClientDisguiseManager.getDisguise(player.getId());
        if (disguise == null || !disguise.isPlayerDisguise()) return null;

        // Prevent accidental recursion: the target profile must differ.
        if (disguise.getTargetPlayerUuid().equals(this.profile.getId())) return null;

        return client.getNetworkHandler().getPlayerListEntry(disguise.getTargetPlayerUuid());
    }
}