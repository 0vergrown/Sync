package dev.overgrown.sync.factory.power.type.prevent_label_render;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PreventLabelRenderPower extends Power {

    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public PreventLabelRenderPower(PowerType<?> type, LivingEntity entity,
                                   Predicate<Entity> entityCondition,
                                   Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
    }

    /**
     * Determines whether the name tag of the power holder should be hidden
     * from a specific viewer.
     *
     * @param viewer the entity looking at the power holder (usually a player)
     * @return true if the label should be hidden for this viewer
     */
    public boolean shouldHideForViewer(Entity viewer) {
        if (viewer == null) {
            return false;
        }

        // No conditions → hide for everyone
        if (entityCondition == null && bientityCondition == null) {
            return true;
        }

        // Check entity condition on the viewer
        if (entityCondition != null && !entityCondition.test(viewer)) {
            return false;
        }

        // Check bi-entity condition between viewer (actor) and holder (target)
        if (bientityCondition != null && !bientityCondition.test(new Pair<>(viewer, entity))) {
            return false;
        }

        return true;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("prevent_label_render"),
                new SerializableData()
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, player) ->
                        new PreventLabelRenderPower(type, player,
                                data.get("entity_condition"),
                                data.get("bientity_condition")))
                .allowCondition();
    }
}