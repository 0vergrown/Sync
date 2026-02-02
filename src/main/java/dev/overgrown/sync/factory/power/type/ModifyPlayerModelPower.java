package dev.overgrown.sync.factory.power.type;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.LivingEntity;

public class ModifyPlayerModelPower extends Power {

    public final Model model;

    public ModifyPlayerModelPower(PowerType<?> type, LivingEntity entity, Model model) {
        super(type, entity);
        this.model = model;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("modify_player_model"),
                new SerializableData()
                        .add("model", SerializableDataType.enumValue(Model.class)),
                data -> (powerType, entity) -> new ModifyPlayerModelPower(
                        powerType,
                        entity,
                        data.get("model"))
        ).allowCondition();
    }

    public enum Model {

        FOUR_ARMS,
        STINKFLY

    }
}
