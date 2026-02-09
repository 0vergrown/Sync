package dev.overgrown.sync.factory.power.type.emissive;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class EmissivePower extends Power {
    public static final String LIGHT = "light";
    public static final String DYNAMIC_LIGHT = "dynamic_light";

    public final int light;
    public final int dynamicLight;

    public EmissivePower(PowerType<?> type, LivingEntity entity, int light, int dynamicLight) {
        super(type, entity);
        this.light = Math.min(Math.max(light, 0), 15);
        this.dynamicLight = Math.min(Math.max(dynamicLight, 0), 15);
    }

    public static PowerFactory<EmissivePower> getFactory() {
        PowerFactory<EmissivePower> factory = new PowerFactory<>(
                Sync.identifier("emissive"),
                new SerializableData()
                        .add(LIGHT, SerializableDataTypes.INT)
                        .add(DYNAMIC_LIGHT, SerializableDataTypes.INT, 0),
                data -> (type, player) -> new EmissivePower(
                        type,
                        player,
                        data.getInt(LIGHT),
                        data.getInt(DYNAMIC_LIGHT)
                )
        );
        factory.allowCondition();
        return factory;
    }
}