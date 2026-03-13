package dev.overgrown.sync.mixin.prevent_taming;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.prevent_taming.PreventTamingPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(targets = "net.minecraft.entity.passive.FoxEntity$MateGoal")
public abstract class FoxEntityMateGoalMixin extends AnimalMateGoal {

    public FoxEntityMateGoalMixin(AnimalEntity animal, double speed) {
        super(animal, speed);
    }

    @ModifyExpressionValue(
            method = "breed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;",
                    ordinal = 0
            )
    )
    private UUID sync$preventTrustedUUIDAddition(UUID original) {
        AnimalEntity fox = this.animal;
        if (fox == null) return original;
        ServerPlayerEntity player = fox.getLovingPlayer();

        List<PreventTamingPower> powers = PowerHolderComponent.getPowers(player, PreventTamingPower.class);
        Optional<PreventTamingPower> preventTamingPower = powers.stream().filter(power -> power.doesApply(fox)).findFirst();

        if (preventTamingPower.isPresent()) {
            preventTamingPower.get().executeAction(fox);
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "breed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;",
                    ordinal = 1
            )
    )
    private UUID sync$preventTrustedUUIDAdditionMate(UUID original) {
        AnimalEntity fox = this.mate;
        if (fox == null) return original;
        ServerPlayerEntity player = fox.getLovingPlayer();

        List<PreventTamingPower> powers = PowerHolderComponent.getPowers(player, PreventTamingPower.class);
        Optional<PreventTamingPower> preventTamingPower = powers.stream().filter(power -> power.doesApply(fox)).findFirst();

        if (preventTamingPower.isPresent()) {
            preventTamingPower.get().executeAction(fox);
            return null;
        }

        return original;
    }
}