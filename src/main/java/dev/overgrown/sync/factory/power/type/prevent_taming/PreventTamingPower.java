package dev.overgrown.sync.factory.power.type.prevent_taming;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventTamingPower extends Power {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public PreventTamingPower(PowerType<?> type, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity other) {
        return this.bientityCondition == null || this.bientityCondition.test(new Pair<>(this.entity, other));
    }

    public void executeAction(Entity other) {
        if (this.biEntityAction != null) {
            this.biEntityAction.accept(new Pair<>(this.entity, other));
        }
    }

    public static PowerFactory<Power> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("prevent_taming"),
                new SerializableData()
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, entity) -> new PreventTamingPower(type, entity, data.get("bientity_action"), data.get("bientity_condition"))
        ).allowCondition();
    }
}