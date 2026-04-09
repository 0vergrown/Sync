package dev.overgrown.sync.factory.data.disguise.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import dev.overgrown.sync.Sync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
 * <p>Skins are loaded asynchronously via {@link net.minecraft.client.texture.SkinProvider}
 * the first time a disguise carrying profile NBT data is received, and evicted when that
 * disguise is removed.</p>
 */
@Environment(EnvType.CLIENT)
public class OfflinePlayerSkinCache {

    private static final Map<UUID, Identifier> SKIN_TEXTURES = new ConcurrentHashMap<>();
    private static final Map<UUID, String> SKIN_MODELS = new ConcurrentHashMap<>();

    /**
     * Reconstructs a {@link GameProfile} from the encoded skin properties stored in
     * {@code profileNbt} and asks the skin provider to load the texture asynchronously.
     * The loaded {@link Identifier} is cached by {@code uuid} for later retrieval.
     *
     * @param uuid       UUID of the offline target player
     * @param profileNbt NBT written by {@code DisguiseAsPlayerAction#buildProfileNbt}
     */
    public static void register(UUID uuid, NbtCompound profileNbt) {
        if (!profileNbt.contains("sync$skin_value")) return;

        String value     = profileNbt.getString("sync$skin_value");
        String signature = profileNbt.contains("sync$skin_signature")
                ? profileNbt.getString("sync$skin_signature")
                : null;

        if (profileNbt.contains("sync$skin_model")) {
            SKIN_MODELS.put(uuid, profileNbt.getString("sync$skin_model"));
        }

        // Build a minimal profile that only carries the textures property.
        // The username "_sync_offline_" is a placeholder; it is never displayed.
        GameProfile profile = new GameProfile(uuid, "_sync_offline_");
        profile.getProperties().put("textures", new Property("textures", value, signature));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        try {
            client.getSkinProvider().loadSkin(profile, (type, id, texture) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    SKIN_TEXTURES.put(uuid, id);
                }
            }, false);
        } catch (Exception e) {
            Sync.LOGGER.warn("[Sync] OfflinePlayerSkinCache: failed to load skin for {}: {}", uuid, e.getMessage());
        }
    }

    /** Returns the cached skin texture identifier, or {@code null} if not yet loaded. */
    @Nullable
    public static Identifier getSkin(UUID uuid) {
        return SKIN_TEXTURES.get(uuid);
    }

    /** Returns the cached model type ({@code "slim"} or {@code "default"}), or {@code null} if unknown. */
    @Nullable
    public static String getModel(UUID uuid) {
        return SKIN_MODELS.get(uuid);
    }

    /** Called when a disguise targeting {@code uuid} is removed. */
    public static void invalidate(UUID uuid) {
        SKIN_TEXTURES.remove(uuid);
        SKIN_MODELS.remove(uuid);
    }

    /** Called on world unload / disconnect to wipe all cached textures. */
    public static void clear() {
        SKIN_TEXTURES.clear();
        SKIN_MODELS.clear();
    }
}
