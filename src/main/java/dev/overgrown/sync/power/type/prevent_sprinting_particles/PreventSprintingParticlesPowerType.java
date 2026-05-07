package dev.overgrown.sync.power.type.prevent_sprinting_particles;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventSprintingParticlesPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventSprintingParticlesPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData(),
        (data, condition) -> new PreventSprintingParticlesPowerType(condition),
        (powerType, serializableData) -> serializableData.instance()
    );

    public PreventSprintingParticlesPowerType(Optional<EntityCondition> condition) {
        super(condition);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.PREVENT_SPRINTING_PARTICLES;
    }
}
