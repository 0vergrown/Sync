package dev.overgrown.sync.factory.action.entity.disguise_as_player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity action that disguises the entity as a named player, even if that player
 * is not currently online. The player's UUID and skin properties are resolved
 * from the server's user cache and session service.
 *
 * <p>Lookup order: if {@code player_uuid} is provided, the profile is resolved
 * by UUID first. Otherwise (or as a fallback), {@code player_name} is used.
 * At least one of the two fields must be present.</p>
 *
 * <p>Type ID: {@code sync:disguise_as_player}</p>
 */
public class DisguiseAsPlayerAction {

    /** Hard-coded entity type ID for players, used as the disguise target type. */
    private static final Identifier PLAYER_TYPE_ID = new Identifier("minecraft", "player");

    private static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            Sync.LOGGER.warn("sync:disguise_as_player – entity '{}' is not a LivingEntity, skipping.",
                    entity.getType().getUntranslatedName());
            return;
        }

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;

        boolean overwrite = data.getBoolean("overwrite");
        if (!overwrite && DisguiseManager.isDisguised(entity.getUuid())) return;

        if (data.isPresent("before_action")) {
            ((ActionFactory<Entity>.Instance) data.get("before_action")).accept(entity);
        }

        MinecraftServer server = serverWorld.getServer();
        Optional<GameProfile> profileOpt = resolveProfile(data, server);

        if (profileOpt.isEmpty()) {
            Sync.LOGGER.warn("sync:disguise_as_player – no cached profile found. " +
                    "The player must have joined this server at least once.");
            return;
        }

        // The user cache only stores UUID + name. We need texture properties for
        // the client to render the correct skin, so fill them from the session service.
        GameProfile profile = server.getSessionService().fillProfileProperties(profileOpt.get(), false);

        NbtCompound profileNbt = buildProfileNbt(profile);

        DisguiseData disguiseData = new DisguiseData(PLAYER_TYPE_ID, -1, profile.getId(), profileNbt);
        DisguiseManager.forceApplyDisguise(living, disguiseData);

        if (data.isPresent("after_action")) {
            ((ActionFactory<Entity>.Instance) data.get("after_action")).accept(entity);
        }
    }

    /**
     * Resolves a {@link GameProfile} by UUID or name. When {@code player_uuid} is
     * provided, a profile is constructed directly from it (no user-cache lookup needed,
     * so the target player does not need to have joined this server). When only
     * {@code player_name} is provided, the user cache is queried instead.
     */
    private static Optional<GameProfile> resolveProfile(SerializableData.Instance data, MinecraftServer server) {
        if (data.isPresent("player_uuid")) {
            String uuidStr = data.getString("player_uuid");
            try {
                UUID uuid = UUID.fromString(uuidStr);
                return Optional.of(new GameProfile(uuid, ""));
            } catch (IllegalArgumentException e) {
                Sync.LOGGER.warn("sync:disguise_as_player – invalid UUID '{}': {}", uuidStr, e.getMessage());
            }
        }

        if (data.isPresent("player_name")) {
            return server.getUserCache().findByName(data.getString("player_name"));
        }

        return Optional.empty();
    }

    /**
     * Serialises the {@code textures} property of the given profile into an
     * {@link NbtCompound} that can be sent to the client inside {@link DisguiseData}.
     * Returns {@code null} when the profile has no texture data (e.g. on
     * offline-mode servers where the profile was never enriched by Mojang auth).
     */
    private static NbtCompound buildProfileNbt(GameProfile profile) {
        Collection<Property> textureProps = profile.getProperties().get("textures");
        if (textureProps == null || textureProps.isEmpty()) return null;

        Property texture = textureProps.iterator().next();
        NbtCompound nbt = new NbtCompound();
        nbt.putString("sync$skin_value", texture.getValue());
        String sig = texture.getSignature();
        if (sig != null && !sig.isEmpty()) {
            nbt.putString("sync$skin_signature", sig);
        }
        nbt.putString("sync$skin_model", parseModelType(texture.getValue()));
        return nbt;
    }

    /**
     * Decodes the Base64 texture value and extracts the model type.
     * Returns {@code "slim"} if the skin metadata specifies it, otherwise {@code "default"}.
     */
    private static String parseModelType(String base64Value) {
        try {
            String json = new String(Base64.getDecoder().decode(base64Value));
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject textures = root.getAsJsonObject("textures");
            if (textures != null && textures.has("SKIN")) {
                JsonObject skin = textures.getAsJsonObject("SKIN");
                if (skin.has("metadata")) {
                    JsonObject metadata = skin.getAsJsonObject("metadata");
                    if (metadata.has("model")) {
                        return metadata.get("model").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            Sync.LOGGER.warn("sync:disguise_as_player – failed to parse skin model type: {}", e.getMessage());
        }
        return "default";
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("disguise_as_player"),
                new SerializableData()
                        .add("player_name", SerializableDataTypes.STRING, null)
                        .add("player_uuid", SerializableDataTypes.STRING, null)
                        .add("overwrite", SerializableDataTypes.BOOLEAN, true)
                        .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("after_action", ApoliDataTypes.ENTITY_ACTION, null),
                DisguiseAsPlayerAction::action
        );
    }
}
