package dev.overgrown.sync.action.type.entity.print;

import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<PrintEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("message", SerializableDataTypes.STRING)
            .add("show_in_chat", SerializableDataTypes.BOOLEAN, false)
            .add("logger_id", SerializableDataTypes.STRING, "Sync/PrintAction"),
        data -> new PrintEntityActionType(
            data.get("message"),
            data.get("show_in_chat"),
            data.get("logger_id")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("message", actionType.message)
            .set("show_in_chat", actionType.showInChat)
            .set("logger_id", actionType.loggerId)
    );

    private final String message;
    private final boolean showInChat;
    private final String loggerId;

    public PrintEntityActionType(String message, boolean showInChat, String loggerId) {
        this.message = message;
        this.showInChat = showInChat;
        this.loggerId = loggerId;
    }

    @Override
    public void accept(EntityActionContext context) {
        Logger logger = LoggerFactory.getLogger(loggerId);
        logger.info(message);

        if (showInChat && context.entity() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal(message), false);
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.PRINT;
    }
}
