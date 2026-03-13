package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class TemptGoalMixin {

    @Shadow @Final protected PathAwareEntity mob;
    @Shadow @Nullable protected PlayerEntity closestPlayer;

    @Inject(
            method = "canBeScared",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventBeingScared(CallbackInfoReturnable<Boolean> cir) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(this.closestPlayer, this.mob);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.hostileOrPassive()) {
                cir.setReturnValue(false);
            }
        }
    }
}