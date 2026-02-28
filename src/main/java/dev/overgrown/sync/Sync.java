package dev.overgrown.sync;

import dev.overgrown.sync.factory.data.keybind.DataDrivenKeybindDefinition;
import dev.overgrown.sync.factory.data.keybind.DataDrivenKeybindLoader;
import dev.overgrown.sync.factory.power.type.action_on_sending_message.ActionOnSendingMessagePower;
import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import dev.overgrown.sync.factory.action.entity.teleportation.events.EntityCleanupHandler;
import dev.overgrown.sync.factory.action.entity.grant_all_powers.GrantAllPowersAction;
import dev.overgrown.sync.factory.action.entity.radial_menu.server.RadialMenuServer;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import dev.overgrown.sync.factory.power.type.action_on_death.ActionOnDeathPower;
import dev.overgrown.sync.registry.factory.SyncTypeRegistry;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import dev.overgrown.sync.factory.condition.entity.player_model_type.utils.PlayerModelTypeManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Prioritized;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

        // Data-driven keybinds
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new DataDrivenKeybindLoader());

        // Register GrantAllPowersAction as a resource reload listener
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new GrantAllPowersAction());

        // Action On Sending Message: Chat messages (player GUI, /say by player, etc.)
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getSignedContent();
            Identifier typeId = sender.getWorld()
                    .getRegistryManager()
                    .get(RegistryKeys.MESSAGE_TYPE)
                    .getId(params.type());
            return processMessagePowers(sender, content, typeId);
        });

        // Action On Sending Message: Command messages (/me, /say from non-player, etc.)
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            if (!(source.getEntity() instanceof ServerPlayerEntity sender)) return true;
            String content = message.getSignedContent();
            Identifier typeId = source.getRegistryManager()
                    .get(RegistryKeys.MESSAGE_TYPE)
                    .getId(params.type());
            return processMessagePowers(sender, content, typeId);
        });

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
            syncKeybindsToPlayer(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    KeyPressManager.removePlayer(handler.player.getUuid());
                    PlayerModelTypeManager.removePlayer(handler.player.getUuid());
                }
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("disguise")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("entity_type", IdentifierArgumentType.identifier())
                            .suggests((ctx, builder) -> {
                                Registries.ENTITY_TYPE.getIds().forEach(id -> {
                                    if (id.toString().startsWith(builder.getRemaining()) || id.getPath().startsWith(builder.getRemaining())) {
                                        builder.suggest(id.toString());
                                    }
                                });
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                Identifier typeId = IdentifierArgumentType.getIdentifier(ctx, "entity_type");
                                EntityType<?> entityType = Registries.ENTITY_TYPE.get(typeId);

                                if (!Registries.ENTITY_TYPE.containsId(typeId)) {
                                    ctx.getSource().sendError(Text.literal("Unknown entity type: " + typeId));
                                    return 0;
                                }

                                Entity dummy = entityType.create(player.getWorld());
                                if (dummy == null) {
                                    ctx.getSource().sendError(Text.literal("Cannot create entity of type: " + typeId));
                                    return 0;
                                }
                                dummy.discard();

                                DisguiseData data = new DisguiseData(typeId, -1, null, Text.literal(typeId.getPath()));
                                DisguiseManager.forceApplyDisguise(player, data);
                                ctx.getSource().sendFeedback(() -> Text.literal("Disguised as " + typeId), false);
                                return 1;
                            }))
                    .then(CommandManager.literal("clear")
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                DisguiseManager.removeDisguise(player);
                                ctx.getSource().sendFeedback(() -> Text.literal("Disguise removed"), false);
                                return 1;
                            })));
        });
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

    /**
     * Evaluates all active {@link ActionOnSendingMessagePower}s on {@code player}
     * in descending priority order.  Returns {@code false} (cancel) as soon as
     * any power decides to block the message.
     */
    private static boolean processMessagePowers(ServerPlayerEntity player,
                                                String content,
                                                Identifier typeId) {
        Prioritized.CallInstance<ActionOnSendingMessagePower> call = new Prioritized.CallInstance<>();
        call.add(player, ActionOnSendingMessagePower.class);

        for (int p = call.getMaxPriority(); p >= call.getMinPriority(); p--) {
            for (ActionOnSendingMessagePower power : call.getPowers(p)) {
                if (!power.onSendMessage(content, typeId)) {
                    return false; // First cancellation wins; stop processing
                }
            }
        }
        return true;
    }

    /**
     * Serializes all currently-loaded data-driven keybind definitions and sends
     * them to {@code player} so the client can register the corresponding
     * {@link net.minecraft.client.option.KeyBinding}s.
     */
    private static void syncKeybindsToPlayer(ServerPlayerEntity player) {
        List<DataDrivenKeybindDefinition> defs = DataDrivenKeybindLoader.LOADED;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(defs.size());
        for (DataDrivenKeybindDefinition def : defs) {
            buf.writeIdentifier(def.id());
            buf.writeString(def.key());
            buf.writeString(def.category());
            buf.writeBoolean(def.name() != null);
            if (def.name() != null) {
                buf.writeString(def.name());
            }
        }

        ServerPlayNetworking.send(player, ModPackets.KEYBIND_SYNC, buf);
        LOGGER.debug("[Sync/Keybinds] Sent {} keybind definition(s) to {}.",
                defs.size(), player.getName().getString());
    }
}