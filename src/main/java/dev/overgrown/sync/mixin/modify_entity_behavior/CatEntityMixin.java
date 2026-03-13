package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CatEntity.class)
public abstract class CatEntityMixin extends TameableEntity implements VariantHolder<CatVariant> {

    protected CatEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "initGoals",
            at = @At(
                    "TAIL"
            )
    )
    private void sync$markPlayerAsTarget(CallbackInfo ci) {
        this.targetSelector.add(1, new UntamedActiveTargetGoal<>(this, PlayerEntity.class, false, livingEntity -> {
            BehaviorHelper behaviorHelper = new BehaviorHelper(livingEntity, this);

            if (behaviorHelper.checkEntity()) {
                return behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.HOSTILE);
            }

            return false;
        }));
    }
}