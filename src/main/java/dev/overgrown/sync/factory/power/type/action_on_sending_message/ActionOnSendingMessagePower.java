package dev.overgrown.sync.factory.power.type.action_on_sending_message;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.action_on_sending_message.utils.MessageConsumer;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.Prioritized;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ActionOnSendingMessagePower extends Power implements Prioritized<ActionOnSendingMessagePower> {

    public static final SerializableDataType<MessageConsumer> MESSAGE_CONSUMER_TYPE =
            SerializableDataType.compound(
                    MessageConsumer.class,
                    new SerializableData()
                            .add("filter", SerializableDataTypes.STRING)
                            .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                            .add("after_action", ApoliDataTypes.ENTITY_ACTION, null)
                            .add("replacement", SerializableDataTypes.STRING, null)
                            .add("prevent", SerializableDataTypes.BOOLEAN, false),
                    data -> new MessageConsumer(
                            data.getString("filter"),
                            data.isPresent("before_action") ? data.get("before_action") : null,
                            data.isPresent("after_action") ? data.get("after_action")  : null,
                            data.isPresent("replacement") ? data.getString("replacement") : null,
                            data.getBoolean("prevent")
                    ),
                    (sd, mc) -> {
                        SerializableData.Instance inst = sd.new Instance();
                        inst.set("filter", mc.getRawPattern());
                        inst.set("before_action", mc.getBeforeAction());
                        inst.set("after_action", mc.getAfterAction());
                        inst.set("replacement", mc.getReplacement());
                        inst.set("prevent", mc.shouldPrevent());
                        return inst;
                    }
            );

    public static final SerializableDataType<List<MessageConsumer>> MESSAGE_CONSUMERS_TYPE =
            SerializableDataType.list(MESSAGE_CONSUMER_TYPE);

    @Nullable
    private final Identifier messageTypeId;
    private final List<MessageConsumer> consumers;
    private final int priority;

    public ActionOnSendingMessagePower(PowerType<?> type,
                                       LivingEntity entity,
                                       @Nullable Identifier messageTypeId,
                                       List<MessageConsumer> consumers,
                                       int priority) {
        super(type, entity);
        this.messageTypeId = messageTypeId;
        this.consumers = consumers;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * Processes a sent message against this power's filters.
     *
     * @param messageContent the message text
     * @param actualTypeId the message type identifier (e.g. {@code minecraft:chat})
     * @return a {@link Result} describing whether the message is allowed and any text modifications
     */
    public Result processMessage(String messageContent, @Nullable Identifier actualTypeId) {
        if (!isActive()) return Result.ALLOW;

        // Skip if the message type does not match the configured filter
        if (messageTypeId != null && !messageTypeId.equals(actualTypeId)) {
            return Result.ALLOW;
        }

        // No consumers mean the power condition was met but there are no filters to check
        if (consumers.isEmpty()) {
            return Result.ALLOW;
        }

        String currentMessage = messageContent;
        boolean anyMatched = false;
        boolean prevented = false;

        for (MessageConsumer consumer : consumers) {
            if (!consumer.matches(currentMessage)) continue;

            anyMatched = true;

            // Run before_action
            if (consumer.getBeforeAction() != null) {
                consumer.getBeforeAction().accept(entity);
            }

            // Apply replacement if configured
            currentMessage = consumer.applyReplacement(currentMessage);

            // Check prevention
            if (consumer.shouldPrevent()) {
                prevented = true;
            }

            // Run after_action
            if (consumer.getAfterAction() != null) {
                consumer.getAfterAction().accept(entity);
            }

            // If prevented, stop processing further filters in this power
            if (prevented) break;
        }

        if (prevented) {
            return Result.prevent();
        }

        if (anyMatched && !currentMessage.equals(messageContent)) {
            return Result.modify(currentMessage);
        }

        return Result.ALLOW;
    }

    /**
     * The result of processing a message through this power.
     */
    public static final class Result {
        public static final Result ALLOW = new Result(false, null);

        private final boolean prevented;
        @Nullable
        private final String modifiedMessage;

        private Result(boolean prevented, @Nullable String modifiedMessage) {
            this.prevented = prevented;
            this.modifiedMessage = modifiedMessage;
        }

        public static Result prevent() {
            return new Result(true, null);
        }

        public static Result modify(String newMessage) {
            return new Result(false, newMessage);
        }

        public boolean isPrevented() {
            return prevented;
        }

        @Nullable
        public String getModifiedMessage() {
            return modifiedMessage;
        }
    }

    public static PowerFactory<ActionOnSendingMessagePower> getFactory() {
        return new PowerFactory<ActionOnSendingMessagePower>(
                Sync.identifier("action_on_sending_message"),
                new SerializableData()
                        .add("message_type", SerializableDataTypes.IDENTIFIER, null)
                        .add("filter", MESSAGE_CONSUMER_TYPE,  null)
                        .add("filters", MESSAGE_CONSUMERS_TYPE, null)
                        .add("priority", SerializableDataTypes.INT, 0),
                data -> (powerType, entity) -> {
                    Identifier messageTypeId = data.isPresent("message_type")
                            ? data.getId("message_type") : null;

                    List<MessageConsumer> consumers = new ArrayList<>();
                    if (data.isPresent("filter")) {
                        consumers.add(data.get("filter"));
                    }
                    if (data.isPresent("filters")) {
                        consumers.addAll(data.get("filters"));
                    }

                    return new ActionOnSendingMessagePower(
                            powerType, entity,
                            messageTypeId, consumers,
                            data.getInt("priority")
                    );
                }
        ).allowCondition();
    }
}