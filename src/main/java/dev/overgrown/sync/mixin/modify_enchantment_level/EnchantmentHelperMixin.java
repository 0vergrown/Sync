package dev.overgrown.sync.mixin.modify_enchantment_level;

import dev.overgrown.sync.factory.power.type.modify_enchantment_level.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(
            method = "get(Lnet/minecraft/item/ItemStack;)Ljava/util/Map;",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$modifyGet(ItemStack stack, CallbackInfoReturnable<Map<Enchantment, Integer>> cir) {
        Entity entity = ((EntityLinkedItemStack) (Object) stack).getEntity();
        if (entity != null && ModifyEnchantmentLevelPower.isInEnchantmentMap(entity)) {
            Map<Enchantment, Integer> result = ModifyEnchantmentLevelPower.get(stack, true);
            if (result != null) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = "getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$modifyGetLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Entity entity = ((EntityLinkedItemStack) (Object) stack).getEntity();
        if (entity != null && ModifyEnchantmentLevelPower.isInEnchantmentMap(entity)) {
            int level = ModifyEnchantmentLevelPower.getLevel(enchantment, stack, true);
            cir.setReturnValue(level);
        }
    }

    @Inject(
            method = "getEquipmentLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;)I",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private static void sync$modifyEquipmentLevel(Enchantment enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (ModifyEnchantmentLevelPower.isInEnchantmentMap(entity)) {
            int level = ModifyEnchantmentLevelPower.getEquipmentLevel(enchantment, entity, true);
            cir.setReturnValue(level);
        }
    }
}