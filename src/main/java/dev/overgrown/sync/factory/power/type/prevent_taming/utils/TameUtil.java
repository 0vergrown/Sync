package dev.overgrown.sync.factory.power.type.prevent_taming.utils;

import dev.overgrown.sync.factory.power.type.prevent_taming.PreventTamingPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/**
 * Helper class for preventing taming of entities through the Prevent Taming power.
 */
public class TameUtil {

    private TameUtil() {}

    /**
     * Prevents taming of an entity by a player.
     * @param original The original value of the random roll for taming.
     * @param player The player attempting to tame the entity.
     * @param tameable The entity being tamed.
     * @return The new value of the taming attempt, which is the original value if no power is present, or a non-zero value if a power is present.
     */
    public static int preventTaming(int original, PlayerEntity player, Entity tameable) {
        if (original == 0) {
            List<PreventTamingPower> powers = PowerHolderComponent.getPowers(player, PreventTamingPower.class);
            Optional<PreventTamingPower> preventTamingPower = powers.stream().filter(power -> power.doesApply(tameable)).findFirst();

            if (preventTamingPower.isPresent()) {
                preventTamingPower.get().executeAction(tameable);
                // As long as we return a non-zero value, the taming will fail
                return original + 1;
            }
        }

        return original;
    }

    /**
     * Prevents taming of a horse by a player.
     * @param original Max temper of the horse.
     * @param horse The horse being tamed.
     * @param temper Temper of the horse.
     * @return The new value of the taming attempt, which is max temper if no power is present, or max temper + temper if a power is present.
     */
    public static int preventTamingHorse(int original, AbstractHorseEntity horse, int temper) {
        if (!(horse.getFirstPassenger() instanceof PlayerEntity player)) return original;

        if (original < temper) {
            List<PreventTamingPower> powers = PowerHolderComponent.getPowers(player, PreventTamingPower.class);
            Optional<PreventTamingPower> preventTamingPower = powers.stream().filter(power -> power.doesApply(horse)).findFirst();

            if (preventTamingPower.isPresent()) {
                preventTamingPower.get().executeAction(horse);
                // As long as we return max temper + temper, the taming will fail
                return original + temper;
            }
        }

        return original;
    }

    /**
     * Prevents taming of an entity by a player by injecting in the HEAD and cancelling the method.
     * @param player The player attempting to tame the entity.
     * @param tameable The entity being tamed.
     * @param ci The callback info of the mixin.
     */
    public static void preventTameAction(PlayerEntity player, Entity tameable, CallbackInfo ci) {
        List<PreventTamingPower> powers = PowerHolderComponent.getPowers(player, PreventTamingPower.class);
        Optional<PreventTamingPower> preventTamingPower = powers.stream().filter(power -> power.doesApply(tameable)).findFirst();

        if (preventTamingPower.isPresent()) {
            preventTamingPower.get().executeAction(tameable);
            ci.cancel();
        }
    }
}