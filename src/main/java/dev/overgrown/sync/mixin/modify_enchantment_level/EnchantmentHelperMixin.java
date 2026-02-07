package dev.overgrown.sync.mixin.modify_enchantment_level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.overgrown.sync.factory.power.type.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean sync$forEachIsEmpty(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) (Object) stack).getEntity());
    }

    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList sync$getEnchantmentsOnForEach(NbtList original, @Local(argsOnly = true) ItemStack stack) {
        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
    }

    @ModifyExpressionValue(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean sync$getLevelWhenEmpty(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) (Object) stack).getEntity());
    }

    @ModifyExpressionValue(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList sync$getEnchantmentsOnGetLevel(NbtList original, @Local(argsOnly = true) ItemStack stack) {
        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
    }
}
