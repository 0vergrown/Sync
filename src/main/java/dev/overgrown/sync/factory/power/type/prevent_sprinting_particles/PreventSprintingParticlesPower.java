package dev.overgrown.sync.factory.power.type.prevent_sprinting_particles;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

public class PreventSprintingParticlesPower extends Power {

    public PreventSprintingParticlesPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("prevent_sprinting_particles"),
                new SerializableData(),
                data -> (powerType, entity) -> new PreventSprintingParticlesPower(
                        powerType,
                        entity
                )
        ).allowCondition();
    }
}