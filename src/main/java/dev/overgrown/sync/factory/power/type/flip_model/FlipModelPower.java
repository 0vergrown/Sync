package dev.overgrown.sync.factory.power.type.flip_model;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class FlipModelPower extends Power {

    private final boolean flipView;

    public FlipModelPower(PowerType<?> type, LivingEntity entity, boolean flipView) {
        super(type, entity);
        this.flipView = flipView;
    }

    public boolean shouldFlipView() {
        return this.flipView;
    }

    public static PowerFactory<Power> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("flip_model"),
                new SerializableData()
                        .add("flip_view", SerializableDataTypes.BOOLEAN, false),
                data -> (type, entity) -> new FlipModelPower(type, entity, data.getBoolean("flip_view"))
        ).allowCondition();
    }
}