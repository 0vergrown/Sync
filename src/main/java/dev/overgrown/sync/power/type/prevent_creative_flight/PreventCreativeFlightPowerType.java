package dev.overgrown.sync.power.type.prevent_creative_flight;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventCreativeFlightPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventCreativeFlightPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventCreativeFlightPowerType(
            data.get("entity_action"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
    );

    private final Optional<EntityAction> entityAction;

    public PreventCreativeFlightPowerType(Optional<EntityAction> entityAction, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
        setTicking();
    }

    @Override
    public void serverTick() {
        if (!(getHolder() instanceof ServerPlayerEntity player)) return;
        if (player.isCreative() || player.isSpectator()) return;

        boolean wasFlying = player.getAbilities().flying;
        boolean hadAllowFlying = player.getAbilities().allowFlying;

        if (wasFlying || hadAllowFlying) {
            if (wasFlying) {
                entityAction.ifPresent(action -> action.execute(player));
            }
            player.getAbilities().flying = false;
            player.getAbilities().allowFlying = false;
            player.sendAbilitiesUpdate();
        }
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.PREVENT_CREATIVE_FLIGHT;
    }
}
