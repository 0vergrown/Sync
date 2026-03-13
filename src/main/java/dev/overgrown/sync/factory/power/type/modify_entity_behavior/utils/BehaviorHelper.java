package dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.mixin.modify_entity_behavior.accessor.MobEntityAccessor;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

/**
 * Helper class for applying behavior modifications to entities through the Modify Entity Behavior (Power Type).
 */
public class BehaviorHelper {

    private final Entity mob;
    private final List<ModifyEntityBehaviorPower> powers;

    /**
     * @param user The entity that has the Modify Entity Behavior (Power Type).
     * @param mob The entity that is being modified.
     */
    public BehaviorHelper(Entity user, Entity mob) {
        this.mob = mob;
        this.powers = PowerHolderComponent.getPowers(user, ModifyEntityBehaviorPower.class);
    }

    /**
     * Checks if the Modify Entity Behavior power's conditions apply to the mob.
     * @return True if the Modify Entity Behavior power's conditions apply to the mob.
     */
    public boolean checkEntity() {
        this.powers.removeIf(power -> !power.checkEntity(this.mob));
        return !powers.isEmpty();
    }

    /**
     * Checks if the Modify Entity Behavior power's desired behavior matches the given behavior.
     * @param entityBehavior The behavior to check against.
     * @return True if the Modify Entity Behavior power's desired behavior matches the given behavior.
     */
    public boolean behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior entityBehavior) {
        if (this.powers.isEmpty()) return false;
        return this.powers.stream().findFirst().get().getDesiredBehavior().equals(entityBehavior);
    }

    /**
     * Checks if the Modify Entity Behavior power's desired behavior is not hostile.
     * @return True if either neutral or passive.
     */
    public boolean neutralOrPassive() {
        return this.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.NEUTRAL) || this.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE);
    }

    /**
     * Checks if the Modify Entity Behavior power's desired behavior is not neutral.
     * @return True if either hostile or passive.
     */
    public boolean hostileOrPassive() {
        return this.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.HOSTILE) || this.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE);
    }

    /**
     * Checks if the Modify Entity Behavior power's desired behavior is not neutral or passive, but also isn't explicitly hostile.
     * @return True if neither neutral nor passive is set.
     */
    public boolean neitherNeutralNorPassive() {
        return !this.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.NEUTRAL) && !this.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE);
    }

    /**
     * Adds a new target goal to the mob that targets players with a Modify Entity Behavior power that has the desired behavior set to hostile.
     * @param mob The mob to add the target goal to.
     */
    public static void targetPlayer(MobEntity mob) {
        ((MobEntityAccessor) mob).targetSelector().add(1, new ActiveTargetGoal<>(mob, PlayerEntity.class, false, livingEntity -> {
            BehaviorHelper behaviorHelper = new BehaviorHelper(livingEntity, mob);

            if (behaviorHelper.checkEntity()) {
                return behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.HOSTILE);
            }

            return false;
        }));
    }
}