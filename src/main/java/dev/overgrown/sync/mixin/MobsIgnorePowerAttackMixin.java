package dev.overgrown.sync.mixin;

import dev.overgrown.sync.factory.power.type.MobsIgnorePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MobsIgnorePowerAttackMixin {

    @Inject(method = "onAttacking", at = @At("HEAD"))
    private void sync$onAttacking(Entity target, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        // Only handle player attacks
        if (self instanceof PlayerEntity player) {
            // Only handle when attacking mobs
            if (target instanceof LivingEntity && !(target instanceof PlayerEntity)) {
                PowerHolderComponent.getPowers(player, MobsIgnorePower.class).forEach(power -> {
                    // Only provoke if the power is provokable and the mob would normally be ignored
                    if (power.isProvokable() && power.shouldIgnore(target)) {
                        power.provokeMob(target);
                    }
                });
            }
        }
    }

    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void sync$onDamaged(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        // Handle when this entity is damaged by a player
        if (damageSource.getAttacker() instanceof PlayerEntity player) {
            PowerHolderComponent.getPowers(player, MobsIgnorePower.class).forEach(power -> {
                // Only provoke if the power is provokable and the mob would normally be ignored
                if (power.isProvokable() && power.shouldIgnore(self)) {
                    power.provokeMob(self);
                }
            });
        }
    }
}