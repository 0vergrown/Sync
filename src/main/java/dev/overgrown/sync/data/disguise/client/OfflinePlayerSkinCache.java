package dev.overgrown.sync.data.disguise.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.overgrown.sync.Sync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of skin {@link Identifier}s for players who are not currently
 * in the server's tab list (i.e. offline players used in {@code sync:disguise_as_player}).
 *
 * <p>Skins are loaded asynchronously via the player skin provider the first time a
 * disguise carrying profile NBT data is received, and evicted when that disguise is removed.
 */
@Environment(EnvType.CLIENT)
public class OfflinePlayerSkinCache {

    private static final Map<UUID, Identifier> SKIN_TEXTURES = new ConcurrentHashMap<>();
    private static final Map<UUID, SkinTextures.Model> SKIN_MODELS = new ConcurrentHashMap<>();

    public static void register(UUID uuid, NbtCompound profileNbt) {
        if (!profileNbt.contains("sync$skin_value")) return;

        String value = profileNbt.getString("sync$skin_value");
        String signature = profileNbt.contains("sync$skin_signature")
            ? profileNbt.getString("sync$skin_signature")
            : null;

        if (profileNbt.contains("sync$skin_model")) {
            SKIN_MODELS.put(uuid, SkinTextures.Model.fromName(profileNbt.getString("sync$skin_model")));
        }

        GameProfile profile = new GameProfile(uuid, "_sync_offline_");
        profile.getProperties().put("textures", new Property("textures", value, signature));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        try {
            client.getSkinProvider().fetchSkinTextures(profile).thenAccept(textures -> {
                if (textures != null) {
                    SKIN_TEXTURES.put(uuid, textures.texture());
                    if (!SKIN_MODELS.containsKey(uuid)) {
                        SKIN_MODELS.put(uuid, textures.model());
                    }
                }
            });
        } catch (Exception e) {
            Sync.LOGGER.warn("[Sync] OfflinePlayerSkinCache: failed to load skin for {}: {}", uuid, e.getMessage());
        }
    }

    @Nullable
    public static Identifier getSkin(UUID uuid) {
        return SKIN_TEXTURES.get(uuid);
    }

    @Nullable
    public static SkinTextures.Model getModel(UUID uuid) {
        return SKIN_MODELS.get(uuid);
    }

    public static void invalidate(UUID uuid) {
        SKIN_TEXTURES.remove(uuid);
        SKIN_MODELS.remove(uuid);
    }

    public static void clear() {
        SKIN_TEXTURES.clear();
        SKIN_MODELS.clear();
    }
}
