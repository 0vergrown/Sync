package dev.overgrown.sync.factory.power.type.energy_swirl;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class EnergySwirlPower extends Power {
    private final Identifier textureLocation;
    private final float size;
    private final float speed;

    public EnergySwirlPower(PowerType<?> type, LivingEntity entity, Identifier textureLocation, float size, float speed) {
        super(type, entity);
        this.textureLocation = textureLocation;
        this.size = size;
        this.speed = speed;
    }

    public Identifier getTextureLocation() {
        return textureLocation;
    }

    public float getSize() {
        return size;
    }

    public float getSpeed() {
        return speed;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("energy_swirl"),
                new SerializableData()
                        .add("texture_location", SerializableDataTypes.IDENTIFIER, null)
                        .add("size", SerializableDataTypes.FLOAT, 1.0f)
                        .add("speed", SerializableDataTypes.FLOAT, 0.01f),
                data -> (powerType, entity) -> new EnergySwirlPower(
                        powerType,
                        entity,
                        data.getId("texture_location"),
                        data.getFloat("size"),
                        data.getFloat("speed")
                )
        ).allowCondition();
    }
}