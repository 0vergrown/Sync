package dev.overgrown.sync.factory.power.type.modify_entity_behavior;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ModifyEntityBehaviorPower extends Power {

    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final EntityBehavior desiredBehavior;

    public ModifyEntityBehaviorPower(PowerType<?> type, LivingEntity player, EntityBehavior desiredBehavior,
                               Predicate<Entity> entityCondition, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(type, player);
        this.entityCondition = entityCondition;
        this.biEntityCondition = biEntityCondition;
        this.desiredBehavior = desiredBehavior;
    }

    public boolean checkEntity(Entity mob) {
        return (this.entityCondition == null || this.entityCondition.test(mob)) && (this.biEntityCondition == null || this.biEntityCondition.test(new Pair<>(entity, mob)));
    }

    public EntityBehavior getDesiredBehavior() {
        return this.desiredBehavior;
    }

    public enum EntityBehavior {
        HOSTILE,
        NEUTRAL,
        PASSIVE
    }

    public static PowerFactory<Power> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("modify_entity_behavior"),
                new SerializableData()
                        .add("behavior", SerializableDataType.enumValue(ModifyEntityBehaviorPower.EntityBehavior.class))
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, entity) -> new ModifyEntityBehaviorPower(type, entity, data.get("behavior"), data.get("entity_condition"), data.get("bientity_condition"))
        ).allowCondition();
    }
}