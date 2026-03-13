package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EscapeDangerGoal.class)
public class EscapeDangerGoalMixin {

    @Shadow @Final protected PathAwareEntity mob;

    @Inject(
            method = "isInDanger",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventEscaping(CallbackInfoReturnable<Boolean> cir) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(this.mob.getAttacker(), this.mob);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE)) {
                cir.setReturnValue(false);
            }
        }
    }
}