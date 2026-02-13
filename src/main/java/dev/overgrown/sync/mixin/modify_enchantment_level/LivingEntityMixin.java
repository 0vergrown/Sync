package dev.overgrown.sync.mixin.modify_enchantment_level;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "getStackInHand",
            at = @At(
                    "RETURN"
            )
    )
    private void sync$linkStackInHand(Hand hand, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (!stack.isEmpty()) {
            ((EntityLinkedItemStack) (Object) stack).setEntity((LivingEntity) (Object) this);
        }
    }
}