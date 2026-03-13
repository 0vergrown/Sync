package dev.overgrown.sync.mixin.prevent_taming;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.prevent_taming.utils.TameUtil;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HorseBondWithPlayerGoal.class)
public class HorseBondWithPlayerGoalMixin {

    @Shadow @Final private AbstractHorseEntity horse;

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/random/Random;nextInt(I)I",
                    ordinal = 1
            )
    )
    private int sync$preventTaming(int original) {
        return TameUtil.preventTamingHorse(original, this.horse, this.horse.getTemper());
    }
}