package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.entity.ai.goal.EmptyAttackGoal;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PufferfishEntity.class)
public abstract class PufferFishEntityMixin extends FishEntity {

    public PufferFishEntityMixin(EntityType<? extends FishEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "initGoals",
            at = @At(
                    "TAIL"
            )
    )
    private void sync$swimTowardsPlayer(CallbackInfo ci) {
        this.goalSelector.add(2, new EmptyAttackGoal(this, 1.0, true));
        BehaviorHelper.targetPlayer(this);
    }

    @Inject(
            method = "onPlayerCollision",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventSting(PlayerEntity player, CallbackInfo ci) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(player, this);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE)) {
                ci.cancel();
            }
        }
    }
}