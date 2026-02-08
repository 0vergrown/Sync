package dev.overgrown.sync.mixin.edible_item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.access.PotentiallyEdibleItemStack;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = HungerManager.class, priority = 900)
public class HungerManagerMixin {

    @ModifyExpressionValue(method = "eat(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isFood()Z"))
    private boolean sync$allowConsumingStack(boolean original, Item item, ItemStack stack) {
        return original || ((PotentiallyEdibleItemStack) (Object) stack).sync$getFoodComponent().isPresent();
    }

    @ModifyExpressionValue(method = "eat(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getFoodComponent()Lnet/minecraft/item/FoodComponent;"))
    private FoodComponent sync$getOrReplaceFoodComponent(FoodComponent original, Item item, ItemStack stack) {
        return ((PotentiallyEdibleItemStack) (Object) stack)
            .sync$getFoodComponent()
            .orElse(original);
    }
}
