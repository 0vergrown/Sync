package dev.overgrown.sync.mixin.body_part_damage_modifier;

import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.BodyPartDamageModifierPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class ModifyDamageByBodyPartMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    "HEAD"
            ),
            ordinal = 0,
            argsOnly = true
    )
    private float sync$applyBodyPartModifiers(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getWorld().isClient) return amount;

        List<BodyPartDamageModifierPower> powers =
                PowerHolderComponent.getPowers(self, BodyPartDamageModifierPower.class);
        for (BodyPartDamageModifierPower power : powers) {
            amount = power.apply(amount, source);
        }
        return amount;
    }
}