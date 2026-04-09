package dev.overgrown.sync.mixin.edible_item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.overgrown.sync.factory.power.type.edible_item.access.PotentiallyEdibleItemStack;
import dev.overgrown.sync.factory.power.type.edible_item.EdibleItemPower;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements PotentiallyEdibleItemStack {

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract ItemStack copy();

    @Unique
    private Optional<EdibleItemPower> sync$getEdiblePower() {
        Entity entity = ((EntityLinkedItemStack) (Object) this).getEntity();
        if (entity == null) return Optional.empty();

        EdibleItemPower edibleItemPower = PowerHolderComponent.getPowers(entity, EdibleItemPower.class)
            .stream()
            .filter(p -> p.doesApply((ItemStack) (Object) this))
            .max(Comparator.comparing(EdibleItemPower::getPriority))
            .orElse(null);

        if (edibleItemPower == null || (this.getItem().isFood() && edibleItemPower.getPriority() <= 0)) {
            return Optional.empty();
        }

        return Optional.of(edibleItemPower);
    }

    @Override
    public Optional<FoodComponent> sync$getFoodComponent() {
        return sync$getEdiblePower().map(EdibleItemPower::getFoodComponent);
    }

    @ModifyReturnValue(method = "getUseAction", at = @At("RETURN"))
    private UseAction sync$replaceUseAction(UseAction original) {
        return sync$getEdiblePower()
            .map(p -> p.getConsumeAnimation().getAction())
            .orElse(original);
    }

    @ModifyReturnValue(method = "getEatSound", at = @At("RETURN"))
    private SoundEvent sync$replaceEatingSound(SoundEvent original) {
        return sync$getEdiblePower()
            .map(EdibleItemPower::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getDrinkSound", at = @At("RETURN"))
    private SoundEvent sync$replaceDrinkingSound(SoundEvent original) {
        return sync$getEdiblePower()
            .map(EdibleItemPower::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getMaxUseTime", at = @At("RETURN"))
    private int sync$modifyMaxConsumingTime(int original) {
        return sync$getEdiblePower()
            .map(EdibleItemPower::getConsumingTime)
            .orElse(original);
    }

    @ModifyExpressionValue(method = "isUsedOnRelease", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isUsedOnRelease(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean sync$disableUseOnRelease(boolean original) {
        return sync$getEdiblePower().isEmpty() && original;
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> sync$consumeCustomFood(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        EdibleItemPower edibleItemPower = sync$getEdiblePower().orElse(null);
        if (edibleItemPower == null) return original.call(instance, world, user, hand);

        ItemStack stackInHand = user.getStackInHand(hand);
        if (!user.canConsume(edibleItemPower.getFoodComponent().isAlwaysEdible())) {
            return original.call(instance, world, user, hand);
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stackInHand);
    }

    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult sync$consumeUsableOnBlockCustomFood(Item instance, ItemUsageContext context, Operation<ActionResult> original) {
        PlayerEntity user = context.getPlayer();
        EdibleItemPower edibleItemPower = sync$getEdiblePower().orElse(null);

        if (user == null || edibleItemPower == null || !user.canConsume(edibleItemPower.getFoodComponent().isAlwaysEdible())) {
            return original.call(instance, context);
        }

        user.setCurrentHand(context.getHand());
        return ActionResult.CONSUME;
    }

    @WrapOperation(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack sync$finishConsumingCustomFood(Item instance, ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original) {
        EdibleItemPower edibleItemPower = sync$getEdiblePower().orElse(null);
        if (edibleItemPower == null) return original.call(instance, stack, world, user);

        edibleItemPower.applyEffects();
        edibleItemPower.executeEntityAction();

        ItemStack copy = this.copy();
        ((EntityLinkedItemStack) (Object) copy).setEntity(user);
        ItemStack consumed = user.eatFood(world, copy);
        ItemStack result = edibleItemPower.executeItemActions(consumed);

        if (!result.isEmpty()) {
            if (consumed.isEmpty()) {
                return result;
            }
            if (ItemStack.canCombine(result, consumed)) {
                consumed.increment(1);
            } else if (user instanceof PlayerEntity playerEntity && !playerEntity.isCreative()) {
                playerEntity.getInventory().offerOrDrop(result);
            } else if (!(user instanceof PlayerEntity)) {
                InventoryUtil.throwItem(user, result, false, false);
            }
        }

        return consumed;
    }
}
