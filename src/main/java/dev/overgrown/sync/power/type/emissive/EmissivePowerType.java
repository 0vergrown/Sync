package dev.overgrown.sync.power.type.emissive;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EmissivePowerType extends PowerType {

    public static final String LIGHT = "light";
    public static final String DYNAMIC_LIGHT = "dynamic_light";

    public static final TypedDataObjectFactory<EmissivePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add(LIGHT, SerializableDataTypes.INT)
            .add(DYNAMIC_LIGHT, SerializableDataTypes.INT, 0),
        (data, condition) -> new EmissivePowerType(
            data.get(LIGHT),
            data.get(DYNAMIC_LIGHT),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set(LIGHT, powerType.light)
            .set(DYNAMIC_LIGHT, powerType.dynamicLight)
    );

    public final int light;
    public final int dynamicLight;

    public EmissivePowerType(int light, int dynamicLight, Optional<EntityCondition> condition) {
        super(condition);
        this.light = Math.min(Math.max(light, 0), 15);
        this.dynamicLight = Math.min(Math.max(dynamicLight, 0), 15);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.EMISSIVE;
    }
}
