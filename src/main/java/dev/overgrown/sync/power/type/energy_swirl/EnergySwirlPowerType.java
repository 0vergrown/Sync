package dev.overgrown.sync.power.type.energy_swirl;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EnergySwirlPowerType extends PowerType {

    public static final TypedDataObjectFactory<EnergySwirlPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("texture_location", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("size", SerializableDataTypes.FLOAT, 1.0f)
            .add("speed", SerializableDataTypes.FLOAT, 0.01f),
        (data, condition) -> new EnergySwirlPowerType(
            data.get("texture_location"),
            data.get("size"),
            data.get("speed"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("texture_location", powerType.textureLocation)
            .set("size", powerType.size)
            .set("speed", powerType.speed)
    );

    private final Optional<Identifier> textureLocation;
    private final float size;
    private final float speed;

    public EnergySwirlPowerType(Optional<Identifier> textureLocation, float size, float speed,
                                Optional<EntityCondition> condition) {
        super(condition);
        this.textureLocation = textureLocation;
        this.size = size;
        this.speed = speed;
    }

    public Identifier getTextureLocation() {
        return textureLocation.orElse(null);
    }

    public float getSize() {
        return size;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.ENERGY_SWIRL;
    }
}
