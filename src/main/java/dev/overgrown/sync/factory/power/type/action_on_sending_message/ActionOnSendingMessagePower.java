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
                            .add("filter",        SerializableDataTypes.STRING)
                            .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                            .add("after_action",  ApoliDataTypes.ENTITY_ACTION, null),
                    data -> new MessageConsumer(
                            data.getString("filter"),
                            data.isPresent("before_action") ? data.get("before_action") : null,
                            data.isPresent("after_action")  ? data.get("after_action")  : null
                    ),
                    (sd, mc) -> {
                        SerializableData.Instance inst = sd.new Instance();
                        inst.set("filter",        mc.getRawPattern());
                        inst.set("before_action", mc.getBeforeAction());
                        inst.set("after_action",  mc.getAfterAction());
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
        this.consumers     = consumers;
        this.priority      = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean onSendMessage(String messageContent, @Nullable Identifier actualTypeId) {
        if (!isActive()) return true;

        // Skip if the message type does not match the configured filter
        if (messageTypeId != null && !messageTypeId.equals(actualTypeId)) {
            return true;
        }

        // No consumers? Then power applies globally to the matched type, but has no filter rules to evaluate and nothing to cancel.
        if (consumers.isEmpty()) {
            return true;
        }

        for (MessageConsumer consumer : consumers) {
            if (consumer.matches(messageContent)) {
                // Filter matched? Then run before_action, then cancel
                if (consumer.getBeforeAction() != null) {
                    consumer.getBeforeAction().accept(entity);
                }
                return false; // Message canceled; stop evaluating further consumers
            }
        }

        // No consumer matched -> message is allowed; run after_actions for every
        // consumer that did NOT match (they all didn't match at this point)
        for (MessageConsumer consumer : consumers) {
            if (consumer.getAfterAction() != null) {
                consumer.getAfterAction().accept(entity);
            }
        }

        return true;
    }

    public static PowerFactory<ActionOnSendingMessagePower> getFactory() {
        return new PowerFactory<ActionOnSendingMessagePower>(
                Sync.identifier("action_on_sending_message"),
                new SerializableData()
                        .add("message_type", SerializableDataTypes.IDENTIFIER, null)
                        .add("filter",       MESSAGE_CONSUMER_TYPE,  null)
                        .add("filters",      MESSAGE_CONSUMERS_TYPE, null)
                        .add("priority",     SerializableDataTypes.INT, 0),
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