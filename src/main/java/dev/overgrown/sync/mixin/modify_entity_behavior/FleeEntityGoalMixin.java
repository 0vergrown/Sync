package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(FleeEntityGoal.class)
public class FleeEntityGoalMixin {

    @Shadow @Final protected PathAwareEntity mob;

    @ModifyArg(
            method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;Ljava/lang/Class;Ljava/util/function/Predicate;FDDLjava/util/function/Predicate;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/TargetPredicate;setPredicate(Ljava/util/function/Predicate;)Lnet/minecraft/entity/ai/TargetPredicate;"
            )
    )
    private Predicate<LivingEntity> sync$preventFleeing(@Nullable Predicate<LivingEntity> original) {
        Predicate<LivingEntity> predicate = target -> {
            BehaviorHelper behaviorHelper = new BehaviorHelper(target, this.mob);
            if (behaviorHelper.checkEntity()) {
                return behaviorHelper.neitherNeutralNorPassive();
            }

            return true;
        };

        if (original == null) {
            original = predicate;
        } else {
            original = original.and(predicate);
        }

        return original;
    }
}