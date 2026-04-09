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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

/**
 * Entity action that disguises the entity as a named player, even if that player
 * is not currently online. The player's UUID (and skin properties, if cached by
 * the server) are resolved from the server's user cache.
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

        String playerName = data.getString("player_name");
        Optional<GameProfile> profileOpt = serverWorld.getServer().getUserCache().findByName(playerName);

        if (profileOpt.isEmpty()) {
            Sync.LOGGER.warn("sync:disguise_as_player – no cached profile found for '{}'. " +
                    "The player must have joined this server at least once.", playerName);
            return;
        }

        GameProfile profile = profileOpt.get();

        // Encode the skin texture property so the client can render the correct skin
        // even when the target player is offline (not in the tab list).
        NbtCompound profileNbt = buildProfileNbt(profile);

        DisguiseData disguiseData = new DisguiseData(PLAYER_TYPE_ID, -1, profile.getId(), profileNbt);
        DisguiseManager.forceApplyDisguise(living, disguiseData);

        if (data.isPresent("after_action")) {
            ((ActionFactory<Entity>.Instance) data.get("after_action")).accept(entity);
        }
    }

    /**
     * Serialises the {@code textures} property of the given profile into an
     * {@link NbtCompound} that can be sent to the client inside {@link DisguiseData}.
     * Returns {@code null} when the profile has no cached texture data (e.g. on
     * offline-mode servers where the profile was never enriched by Mojang auth).
     */
    private static NbtCompound buildProfileNbt(GameProfile profile) {
        Collection<Property> textureProps = profile.getProperties().get("textures");
        if (textureProps == null || textureProps.isEmpty()) return null;

        Property texture = textureProps.iterator().next();
        NbtCompound nbt = new NbtCompound();
        nbt.putString("sync$skin_value", texture.value());
        String sig = texture.signature();
        if (sig != null && !sig.isEmpty()) {
            nbt.putString("sync$skin_signature", sig);
        }
        return nbt;
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("disguise_as_player"),
                new SerializableData()
                        .add("player_name", SerializableDataTypes.STRING)
                        .add("overwrite", SerializableDataTypes.BOOLEAN, true)
                        .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("after_action", ApoliDataTypes.ENTITY_ACTION, null),
                DisguiseAsPlayerAction::action
        );
    }
}
