package dev.overgrown.sync;

import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import dev.overgrown.sync.factory.action.entity.teleportation.events.EntityCleanupHandler;
import dev.overgrown.sync.factory.action.entity.grant_all_powers.GrantAllPowersAction;
import dev.overgrown.sync.factory.action.entity.radial_menu.server.RadialMenuServer;
import dev.overgrown.sync.factory.disguise.DisguiseData;
import dev.overgrown.sync.factory.disguise.DisguiseManager;
import dev.overgrown.sync.factory.power.type.action_on_death.ActionOnDeathPower;
import dev.overgrown.sync.registry.factory.SyncTypeRegistry;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import dev.overgrown.sync.factory.condition.entity.player_model_type.utils.PlayerModelTypeManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class Sync implements ModInitializer {
    public static final String MOD_ID = "sync";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean HAS_ASPECTSLIB = false; // Flag to check if AspectsLib is present

    public static Identifier identifier(String path) {
        return new Identifier(Sync.MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        HAS_ASPECTSLIB = FabricLoader.getInstance().isModLoaded("aspectslib");
        if (HAS_ASPECTSLIB) {
            LOGGER.info("AspectsLib detected - compatibility enabled");
        }

        NamespaceAlias.addAlias("apoli", MOD_ID);
        SyncTypeRegistry.register();
        RadialMenuServer.register();
        SyncEntityRegistry.register();

        // Register entity cleanup handler
        EntityCleanupHandler.register();

        // Register GrantAllPowersAction as a resource reload listener
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new GrantAllPowersAction());

        // Register death event handler
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity) {
                PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                component.getPowers(ActionOnDeathPower.class).forEach(power ->
                        power.onDeath(damageSource, entity.getMaxHealth())); // Use max health as damage amount
            }
            // Clean up disguise when an entity dies
            DisguiseManager.removePlayer(entity.getUuid());
        });

        ServerPlayNetworking.registerGlobalReceiver(
                ModPackets.PLAYER_MODEL_TYPE_UPDATE,
                (server, player, handler, buf, responseSender) -> {
                    String modelType = buf.readString();
                    server.execute(() -> PlayerModelTypeManager.setModelType(player, modelType));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ModPackets.KEY_PRESS_UPDATE,
                (server, player, handler, buf, responseSender) -> {
                    String key = buf.readString();
                    boolean pressed = buf.readBoolean();
                    server.execute(() -> KeyPressManager.updateKeyState(player.getUuid(), key, pressed));
                }
        );

        // Connect / Disconnect
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
            // Send all active disguises to the newly joined player so they
            // immediately see the correct appearance of everyone around them.
            server.execute(() -> syncDisguisesToPlayer(handler.player, server));
        });

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    KeyPressManager.removePlayer(handler.player.getUuid());
                    PlayerModelTypeManager.removePlayer(handler.player.getUuid());
                }
        );
    }

    /**
     * Sends every currently-active disguise to {@code recipient} so they render
     * correctly when the player first loads the world / their surroundings.
     */
    private static void syncDisguisesToPlayer(ServerPlayerEntity recipient, MinecraftServer server) {
        Map<UUID, DisguiseData> all = DisguiseManager.getAllDisguises();
        if (all.isEmpty()) return;

        // Iterate all loaded entities across all dimensions.
        server.getWorlds().forEach(world -> {
            world.iterateEntities().forEach(entity -> {
                DisguiseData data = all.get(entity.getUuid());
                if (data != null) {
                    PacketByteBuf buf = DisguiseManager.buildSetPacket(entity.getId(), data);
                    ServerPlayNetworking.send(recipient, ModPackets.DISGUISE_UPDATE, buf);
                }
            });
        });
    }
}