package dev.overgrown.sync.action.type.entity.disguise_as_player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.disguise.DisguiseData;
import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity action that disguises a living entity as a named or UUID-identified player.
 *
 * <p>1.21 rewrite: uses {@link MinecraftSessionService#fetchProfile} (which returns a
 * {@link ProfileResult}) instead of the removed {@code fillProfileProperties}, and
 * {@link net.minecraft.util.UserCache#findByName} for name-based lookup.</p>
 */
public class DisguiseAsPlayerEntityActionType extends EntityActionType {

    private static final Identifier PLAYER_TYPE_ID = Identifier.of("minecraft", "player");

    public static final TypedDataObjectFactory<DisguiseAsPlayerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("player_name", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("player_uuid", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("overwrite", SerializableDataTypes.BOOLEAN, true)
            .add("before_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("after_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new DisguiseAsPlayerEntityActionType(
            data.get("player_name"),
            data.get("player_uuid"),
            data.get("overwrite"),
            data.get("before_action"),
            data.get("after_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("player_name", actionType.playerName)
            .set("player_uuid", actionType.playerUuid)
            .set("overwrite", actionType.overwrite)
            .set("before_action", actionType.beforeAction)
            .set("after_action", actionType.afterAction)
    );

    private final Optional<String> playerName;
    private final Optional<String> playerUuid;
    private final boolean overwrite;
    private final Optional<EntityAction> beforeAction;
    private final Optional<EntityAction> afterAction;

    public DisguiseAsPlayerEntityActionType(Optional<String> playerName, Optional<String> playerUuid, boolean overwrite,
                                            Optional<EntityAction> beforeAction, Optional<EntityAction> afterAction) {
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.overwrite = overwrite;
        this.beforeAction = beforeAction;
        this.afterAction = afterAction;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        if (!(entity instanceof LivingEntity living)) {
            Sync.LOGGER.warn("sync:disguise_as_player – entity '{}' is not a LivingEntity, skipping.",
                entity.getType().getUntranslatedName());
            return;
        }
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        if (!overwrite && DisguiseManager.isDisguised(entity.getUuid())) return;

        beforeAction.ifPresent(a -> a.execute(entity));

        MinecraftServer server = serverWorld.getServer();
        Optional<GameProfile> initial = resolveProfile(server);
        if (initial.isEmpty()) {
            Sync.LOGGER.warn("sync:disguise_as_player – no cached profile found.");
            return;
        }

        GameProfile baseProfile = initial.get();
        MinecraftSessionService sessionService = server.getSessionService();
        ProfileResult profileResult = sessionService.fetchProfile(baseProfile.getId(), false);
        GameProfile resolved = profileResult != null ? profileResult.profile() : baseProfile;

        NbtCompound profileNbt = buildProfileNbt(resolved);
        DisguiseData disguiseData = new DisguiseData(PLAYER_TYPE_ID, -1, resolved.getId(), profileNbt);
        DisguiseManager.forceApplyDisguise(living, disguiseData);

        afterAction.ifPresent(a -> a.execute(entity));
    }

    private Optional<GameProfile> resolveProfile(MinecraftServer server) {
        if (playerUuid.isPresent()) {
            try {
                UUID uuid = UUID.fromString(playerUuid.get());
                Optional<GameProfile> cached = server.getUserCache().getByUuid(uuid);
                return cached.isPresent() ? cached : Optional.of(new GameProfile(uuid, ""));
            } catch (IllegalArgumentException e) {
                Sync.LOGGER.warn("sync:disguise_as_player – invalid UUID '{}'", playerUuid.get());
            }
        }
        if (playerName.isPresent()) {
            return server.getUserCache().findByName(playerName.get());
        }
        return Optional.empty();
    }

    private static NbtCompound buildProfileNbt(GameProfile profile) {
        NbtCompound nbt = new NbtCompound();
        String name = profile.getName();
        if (name != null && !name.isEmpty()) {
            nbt.putString("sync$player_name", name);
        }

        Collection<Property> textureProps = profile.getProperties().get("textures");
        if (textureProps != null && !textureProps.isEmpty()) {
            Property texture = textureProps.iterator().next();
            nbt.putString("sync$skin_value", texture.value());
            String sig = texture.signature();
            if (sig != null && !sig.isEmpty()) {
                nbt.putString("sync$skin_signature", sig);
            }
        }
        return nbt.isEmpty() ? null : nbt;
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.DISGUISE_AS_PLAYER;
    }
}
