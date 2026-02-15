package dev.overgrown.sync.mixin.modify_enchantment_level;

import dev.overgrown.sync.factory.power.type.modify_enchantment_level.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackEnchantmentMixin {

    @Inject(
            method = "getEnchantments()Lnet/minecraft/nbt/NbtList;",
            at = @At(
                    "RETURN"
            ),
            cancellable = true
    )
    private void sync$modifyEnchantments(CallbackInfoReturnable<NbtList> cir) {
        ItemStack self = (ItemStack) (Object) this;
        Entity entity = ((EntityLinkedItemStack) (Object) self).getEntity();

        if (entity != null && ModifyEnchantmentLevelPower.isInEnchantmentMap(entity)) {
            NbtList modifiedEnchantments = ModifyEnchantmentLevelPower.getEnchantments(self, cir.getReturnValue());
            cir.setReturnValue(modifiedEnchantments);
        }
    }
}