package dev.overgrown.sync.factory.action.item.cooldown;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class CooldownAction {

    public static ActionFactory<Pair<World, ItemStack>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("cooldown"),
                new SerializableData()
                        .add("ticks", SerializableDataTypes.INT, 20),
                (data, worldAndStack) -> {
                    World world = worldAndStack.getLeft();
                    ItemStack stack = worldAndStack.getRight();
                    if (world.isClient || stack.isEmpty()) return;
                    if (((EntityLinkedItemStack) (Object) stack).getEntity() instanceof PlayerEntity player) {
                        player.getItemCooldownManager().set(stack.getItem(), data.getInt("ticks"));
                    }
                }
        );
    }
}
