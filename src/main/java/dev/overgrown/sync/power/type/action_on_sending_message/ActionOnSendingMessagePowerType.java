package dev.overgrown.sync.power.type.action_on_sending_message;

import dev.overgrown.sync.power.type.action_on_sending_message.util.MessageConsumer;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActionOnSendingMessagePowerType extends PowerType implements Prioritized<ActionOnSendingMessagePowerType> {

    private static final SerializableData CONSUMER_DATA = new SerializableData()
        .add("filter", SerializableDataTypes.STRING)
        .add("before_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
        .add("after_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
        .add("replacement", SerializableDataTypes.STRING.optional(), Optional.empty())
        .add("prevent", SerializableDataTypes.BOOLEAN, false);

    public static final SerializableDataType<MessageConsumer> MESSAGE_CONSUMER_TYPE =
        SerializableDataType.compound(
            CONSUMER_DATA,
            data -> new MessageConsumer(
                data.get("filter"),
                data.get("before_action"),
                data.get("after_action"),
                data.get("replacement"),
                data.get("prevent")
            ),
            (consumer, sd) -> sd.instance()
                .set("filter", consumer.getRawPattern())
                .set("before_action", consumer.getBeforeAction())
                .set("after_action", consumer.getAfterAction())
                .set("replacement", consumer.getReplacement())
                .set("prevent", consumer.shouldPrevent())
        );

    public static final SerializableDataType<List<MessageConsumer>> MESSAGE_CONSUMERS_TYPE =
        MESSAGE_CONSUMER_TYPE.list();

    public static final TypedDataObjectFactory<ActionOnSendingMessagePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("message_type", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("filter", MESSAGE_CONSUMER_TYPE.optional(), Optional.empty())
            .add("filters", MESSAGE_CONSUMERS_TYPE.optional(), Optional.empty())
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ActionOnSendingMessagePowerType(
            data.get("message_type"),
            data.get("filter"),
            data.get("filters"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("message_type", powerType.messageTypeId)
            .set("filter", powerType.singleFilter)
            .set("filters", powerType.multiFilter)
            .set("priority", powerType.priority)
    );

    private final Optional<Identifier> messageTypeId;
    private final Optional<MessageConsumer> singleFilter;
    private final Optional<List<MessageConsumer>> multiFilter;
    private final List<MessageConsumer> consumers;
    private final int priority;

    public ActionOnSendingMessagePowerType(Optional<Identifier> messageTypeId,
                                           Optional<MessageConsumer> singleFilter,
                                           Optional<List<MessageConsumer>> multiFilter,
                                           int priority,
                                           Optional<EntityCondition> condition) {
        super(condition);
        this.messageTypeId = messageTypeId;
        this.singleFilter = singleFilter;
        this.multiFilter = multiFilter;
        this.priority = priority;

        List<MessageConsumer> consumers = new ArrayList<>();
        singleFilter.ifPresent(consumers::add);
        multiFilter.ifPresent(consumers::addAll);
        this.consumers = consumers;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public Result processMessage(String messageContent, Identifier actualTypeId) {
        if (!isActive()) return Result.ALLOW;

        if (messageTypeId.isPresent() && !messageTypeId.get().equals(actualTypeId)) {
            return Result.ALLOW;
        }

        if (consumers.isEmpty()) {
            return Result.ALLOW;
        }

        String currentMessage = messageContent;
        boolean anyMatched = false;
        boolean prevented = false;

        for (MessageConsumer consumer : consumers) {
            if (!consumer.matches(currentMessage)) continue;

            anyMatched = true;

            consumer.getBeforeAction().ifPresent(action -> action.accept(new EntityActionContext(getHolder())));

            currentMessage = consumer.applyReplacement(currentMessage);

            if (consumer.shouldPrevent()) {
                prevented = true;
            }

            consumer.getAfterAction().ifPresent(action -> action.accept(new EntityActionContext(getHolder())));

            if (prevented) break;
        }

        if (prevented) return Result.prevent();
        if (anyMatched && !currentMessage.equals(messageContent)) return Result.modify(currentMessage);
        return Result.ALLOW;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.ACTION_ON_SENDING_MESSAGE;
    }

    public static final class Result {
        public static final Result ALLOW = new Result(false, null);
        private final boolean prevented;
        private final String modifiedMessage;

        private Result(boolean prevented, String modifiedMessage) {
            this.prevented = prevented;
            this.modifiedMessage = modifiedMessage;
        }

        public static Result prevent() { return new Result(true, null); }
        public static Result modify(String newMessage) { return new Result(false, newMessage); }
        public boolean isPrevented() { return prevented; }
        public String getModifiedMessage() { return modifiedMessage; }
    }
}
