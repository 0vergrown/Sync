package dev.overgrown.sync.factory.power.type;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

public class ExtraLimbsPower extends Power {

    public ExtraLimbsPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("extra_limbs"),
                new SerializableData(),
                data -> (powerType, entity) -> new ExtraLimbsPower(
                        powerType,
                        entity)
        ).allowCondition();
    }
}
