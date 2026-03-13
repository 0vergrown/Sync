package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {

    @Inject(
            method = "wearsGoldArmor",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$markPlayerAsWearingGold(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (!(target instanceof PlayerEntity player)) return;

        List<PiglinEntity> list = player.getWorld().getNonSpectatingEntities(PiglinEntity.class, player.getBoundingBox().expand(16.0));
        list.forEach(piglin -> {
            BehaviorHelper behaviorHelper = new BehaviorHelper(player, piglin);

            if (behaviorHelper.checkEntity()) {
                if (behaviorHelper.neutralOrPassive()) {
                    cir.setReturnValue(true);
                    // If piglins are explicitly hostile, don't mark the player as wearing gold
                } else if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.HOSTILE)) {
                    cir.setReturnValue(false);
                }
            }
        });
    }

    @Inject(
            method = "onGuardedBlockInteracted",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$allowGuardedBlock(PlayerEntity player, boolean blockOpen, CallbackInfo ci) {
        List<PiglinEntity> list = player.getWorld().getNonSpectatingEntities(PiglinEntity.class, player.getBoundingBox().expand(16.0));
        list.forEach(piglin -> {
            BehaviorHelper behaviorHelper = new BehaviorHelper(player, piglin);

            if (behaviorHelper.checkEntity()) {
                if (behaviorHelper.neutralOrPassive()) {
                    ci.cancel();
                }
            }
        });
    }

    @Inject(
            method = "tryRevenge",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$lobotomizePiglin(AbstractPiglinEntity piglin, LivingEntity target, CallbackInfo ci) {
        BehaviorHelper behaviorHelper = new BehaviorHelper(target, piglin);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.PASSIVE)) {
                ci.cancel();
            }
        }
    }
}