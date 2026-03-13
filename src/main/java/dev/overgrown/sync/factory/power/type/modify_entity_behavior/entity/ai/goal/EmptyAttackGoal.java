package dev.overgrown.sync.factory.power.type.modify_entity_behavior.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

/**
 * A MeleeAttackGoal that does not attack.
 * <br>
 * Used to make mobs move towards a target without attacking.
 */
public class EmptyAttackGoal extends MeleeAttackGoal {

    public EmptyAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
    }

    protected boolean canAttack(LivingEntity target) {
        return false;
    }
}