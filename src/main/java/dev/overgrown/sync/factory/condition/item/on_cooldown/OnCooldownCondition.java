package dev.overgrown.sync.factory.condition.item.on_cooldown;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class OnCooldownCondition {

    public static boolean condition(SerializableData.Instance data, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (((EntityLinkedItemStack) (Object) stack).getEntity() instanceof PlayerEntity player) {
            return player.getItemCooldownManager().isCoolingDown(stack.getItem());
        }
        return false;
    }

    public static ConditionFactory<ItemStack> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("on_cooldown"),
                new SerializableData(),
                OnCooldownCondition::condition
        );
    }
}
