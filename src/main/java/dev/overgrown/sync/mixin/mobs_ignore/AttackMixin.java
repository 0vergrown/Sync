package dev.overgrown.sync.mixin.mobs_ignore;

import dev.overgrown.sync.power.type.mobs_ignore.MobsIgnorePowerType;
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
public class AttackMixin {

    @Inject(method = "onAttacking", at = @At("HEAD"))
    private void sync$onAttacking(Entity target, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player
            && target instanceof LivingEntity
            && !(target instanceof PlayerEntity)) {
            for (MobsIgnorePowerType power : PowerHolderComponent.getPowerTypes(player, MobsIgnorePowerType.class)) {
                if (power.isProvokable() && power.shouldIgnore(target)) {
                    power.provokeMob(target);
                }
            }
        }
    }

    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void sync$onDamaged(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (damageSource.getAttacker() instanceof PlayerEntity player) {
            for (MobsIgnorePowerType power : PowerHolderComponent.getPowerTypes(player, MobsIgnorePowerType.class)) {
                if (power.isProvokable() && power.shouldIgnore(self)) {
                    power.provokeMob(self);
                }
            }
        }
    }
}
