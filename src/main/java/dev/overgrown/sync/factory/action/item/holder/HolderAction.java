package dev.overgrown.sync.factory.action.item.holder;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class HolderAction {

    @SuppressWarnings("unchecked")
    public static ActionFactory<Pair<World, ItemStack>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("holder_action"),
                new SerializableData()
                        .add("action", ApoliDataTypes.ENTITY_ACTION),
                (data, worldAndStack) -> {
                    ItemStack stack = worldAndStack.getRight();
                    if (stack.isEmpty()) return;
                    Entity holder = ((EntityLinkedItemStack)(Object) stack).getEntity();
                    if (holder == null) return;
                    ((ActionFactory<Entity>.Instance) data.get("action")).accept(holder);
                }
        );
    }
}