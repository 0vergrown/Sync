package dev.overgrown.sync;

import dev.overgrown.sync.action.type.entity.grant_all_powers.SourcePowerRegistry;
import dev.overgrown.sync.action.type.entity.radial_menu.server.RadialMenuServer;
import dev.overgrown.sync.condition.type.entity.perspective.util.PerspectiveManager;
import dev.overgrown.sync.condition.type.entity.player_model_type.util.PlayerModelTypeManager;
import dev.overgrown.sync.data.disguise.DisguiseInit;
import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.data.keybind.KeybindInit;
import dev.overgrown.sync.data.rope.common.RopeInit;
import dev.overgrown.sync.data.suppress_power.SuppressedPowerManager;
import dev.overgrown.sync.data.teleportation.EntityCleanupHandler;
import dev.overgrown.sync.data.transfer.StolenPowerSlotManager;
import dev.overgrown.sync.data.transfer.TransferModeManager;
import dev.overgrown.sync.network.SyncNetworking;
import dev.overgrown.sync.power.type.action_on_sending_message.ActionOnSendingMessagePowerType;
import dev.overgrown.sync.power.type.action_on_sending_message.util.TranslationKeyResolver;
import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import dev.overgrown.sync.registry.SyncBiEntityConditionTypes;
import dev.overgrown.sync.registry.SyncBlockActionTypes;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import dev.overgrown.sync.registry.SyncItemActionTypes;
import dev.overgrown.sync.registry.SyncItemConditionTypes;
import dev.overgrown.sync.registry.SyncEntityRegistry;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.calio.util.CalioResourceConditions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Sync implements ModInitializer {
    public static final String MOD_ID = "sync";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean HAS_ASPECTSLIB = false;
    public static boolean HAS_JADE = false;

    public static Identifier identifier(String path) {
        return Identifier.of(Sync.MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        HAS_ASPECTSLIB = FabricLoader.getInstance().isModLoaded("aspectslib");
        HAS_JADE = FabricLoader.getInstance().isModLoaded("jade");

        TranslationKeyResolver.load();

        CalioResourceConditions.ALIASES.addNamespaceAlias("apoli", MOD_ID);

        SyncEntityConditionTypes.register();
        SyncBiEntityConditionTypes.register();
        SyncItemConditionTypes.register();

        SyncEntityActionTypes.register();
        SyncBiEntityActionTypes.register();
        SyncItemActionTypes.register();
        SyncBlockActionTypes.register();

        SyncPowerTypes.register();
        SyncEntityRegistry.register();

        SourcePowerRegistry.registerClearHook();
        RadialMenuServer.register();
        EntityCleanupHandler.register();

        RopeInit.init();
        DisguiseInit.init();
        KeybindInit.init();
        SyncNetworking.register();

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getSignedContent();
            Identifier typeId = sender.getWorld()
                .getRegistryManager()
                .get(RegistryKeys.MESSAGE_TYPE)
                .getId(params.type().value());
            return processMessagePowers(sender, content, typeId);
        });

        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            if (!(source.getEntity() instanceof ServerPlayerEntity sender)) return true;
            String content = message.getSignedContent();
            Identifier typeId = source.getRegistryManager()
                .get(RegistryKeys.MESSAGE_TYPE)
                .getId(params.type().value());
            return processMessagePowers(sender, content, typeId);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                StolenPowerSlotManager.remove(player.getUuid());
                TransferModeManager.remove(player.getUuid());
            }
            DisguiseManager.removePlayer(entity.getUuid());
            SuppressedPowerManager.removeAll(entity.getUuid());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUuid();
            PerspectiveManager.removePlayer(id);
            PlayerModelTypeManager.removePlayer(id);
            DisguiseManager.removePlayer(id);
            SuppressedPowerManager.removeAll(id);
            StolenPowerSlotManager.remove(id);
            TransferModeManager.remove(id);
        });

        LOGGER.info("Sync initialized.");
    }

    private static boolean processMessagePowers(ServerPlayerEntity player, String content, Identifier typeId) {
        Prioritized.CallInstance<ActionOnSendingMessagePowerType> call = new Prioritized.CallInstance<>();
        PowerHolderComponent.getPowerTypes(player, ActionOnSendingMessagePowerType.class)
            .forEach(call::add);

        if (call.isEmpty()) return true;

        String currentMessage = content;
        boolean prevented = false;

        for (int p = call.getMaxPriority(); p >= call.getMinPriority(); p--) {
            if (!call.hasPowerTypes(p)) continue;
            for (ActionOnSendingMessagePowerType power : call.getPowerTypes(p)) {
                ActionOnSendingMessagePowerType.Result result = power.processMessage(currentMessage, typeId);

                if (result.isPrevented()) {
                    prevented = true;
                    break;
                }

                if (result.getModifiedMessage() != null) {
                    currentMessage = result.getModifiedMessage();
                }
            }
            if (prevented) break;
        }

        if (prevented) return false;

        if (!currentMessage.equals(content)) {
            Text formatted = Text.translatable("chat.type.text",
                player.getDisplayName(), Text.literal(currentMessage));
            player.server.getPlayerManager().broadcast(formatted, false);
            return false;
        }

        return true;
    }
}
