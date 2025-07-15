package dev.overgrown.sync.factory.power.type;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

public class ModelFlipPower extends Power {

    public ModelFlipPower(PowerType<?> powerType, LivingEntity livingEntity) {
        super(powerType, livingEntity);
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("model_flip"),
                new SerializableData(),
                data -> ModelFlipPower::new
        ).allowCondition();
    }

}