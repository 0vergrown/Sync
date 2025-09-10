package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintAction {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sync/PrintAction");

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("print"),
                new SerializableData()
                        .add("message", SerializableDataTypes.STRING)
                        .add("show_in_chat", SerializableDataTypes.BOOLEAN, false),
                PrintAction::execute
        );
    }

    private static void execute(SerializableData.Instance data, Entity entity) {
        String message = data.getString("message");
        boolean showInChat = data.getBoolean("show_in_chat");

        // Log to console
        LOGGER.info(message);

        // Send to player chat if enabled and entity is a player
        if (showInChat && entity instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal(message), false);
        }
    }
}