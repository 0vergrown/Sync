package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity implements Monster {

    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "launchLivingEntities",
            at = @At(
                    "HEAD"
            )
    )
    private void sync$preventLaunching(List<Entity> entities, CallbackInfo ci) {
        List<Entity> players = entities.stream().filter(entity -> entity instanceof PlayerEntity).toList();

        if (!players.isEmpty()) {
            for (Entity target : players) {
                BehaviorHelper behaviorHelper = new BehaviorHelper(target, this);

                if (behaviorHelper.checkEntity()) {
                    if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE) ||
                            (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.NEUTRAL) && this.getAttacker() != target)) {
                        entities.remove(target);
                    }
                }
            }
        }
    }

    @Inject(
            method = "damageLivingEntities",
            at = @At(
                    "HEAD"
            )
    )
    private void sync$preventDamaging(List<Entity> entities, CallbackInfo ci) {
        List<Entity> players = entities.stream().filter(entity -> entity instanceof PlayerEntity).toList();

        if (!players.isEmpty()) {
            for (Entity target : players) {
                BehaviorHelper behaviorHelper = new BehaviorHelper(target, this);

                if (behaviorHelper.checkEntity()) {
                    if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE) ||
                            (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.NEUTRAL) && this.getAttacker() != target)) {
                        entities.remove(target);
                    }
                }
            }
        }
    }
}