package dev.overgrown.sync.factory.power.type.action_on_death;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

public class ActionOnDeathPower extends Power {
    private final ActionFactory<Pair<Entity, Entity>>.Instance bientityAction;
    private final ConditionFactory<Pair<Entity, Entity>>.Instance bientityCondition;
    private final ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition;

    public ActionOnDeathPower(
            PowerType<?> type,
            LivingEntity entity,
            ActionFactory<Pair<Entity, Entity>>.Instance bientityAction,
            ConditionFactory<Pair<Entity, Entity>>.Instance bientityCondition,
            ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition
    ) {
        super(type, entity);
        this.bientityAction = bientityAction;
        this.bientityCondition = bientityCondition;
        this.damageCondition = damageCondition;
    }

    public void onDeath(DamageSource damageSource, float damageAmount) {
        if (!this.isActive()) {
            return;
        }

        Entity killer = damageSource.getAttacker();
        Pair<Entity, Entity> entities = new Pair<>(killer, entity);

        // Check bientity condition if present
        if (bientityCondition != null && !bientityCondition.test(entities)) {
            return;
        }

        // Check damage condition if present
        if (damageCondition != null && !damageCondition.test(new Pair<>(damageSource, damageAmount))) {
            return;
        }

        // Execute the bientity action
        bientityAction.accept(entities);
    }

    public static PowerFactory<ActionOnDeathPower> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("action_on_death"),
                new SerializableData()
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
                data -> (PowerType<ActionOnDeathPower> type, LivingEntity entity) -> new ActionOnDeathPower(
                        type,
                        entity,
                        data.get("bientity_action"),
                        data.get("bientity_condition"),
                        data.get("damage_condition")
                )
        ).allowCondition();
    }
}