package dev.overgrown.sync.mixin.mobs_ignore;

import dev.overgrown.sync.factory.power.type.mobs_ignore.MobsIgnorePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MobEntityMixin {
    @Inject(
            method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$canTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        // Cast 'this' to LivingEntity
        LivingEntity self = (LivingEntity)(Object)this;

        // Only apply if the target is a player
        if (target instanceof PlayerEntity player) {
            // Check if player has any MobsIgnorePower that should ignore this entity
            for (MobsIgnorePower power : PowerHolderComponent.getPowers(player, MobsIgnorePower.class)) {
                if (power.shouldIgnore(self)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}