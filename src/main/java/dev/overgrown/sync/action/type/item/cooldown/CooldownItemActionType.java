package dev.overgrown.sync.action.type.item.cooldown;

import dev.overgrown.sync.registry.SyncItemActionTypes;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CooldownItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<CooldownItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("ticks", SerializableDataTypes.INT, 20),
        data -> new CooldownItemActionType(data.get("ticks")),
        (actionType, serializableData) -> serializableData.instance()
            .set("ticks", actionType.ticks)
    );

    private final int ticks;

    public CooldownItemActionType(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public void accept(ItemActionContext context) {
        ItemStack stack = context.stackReference().get();
        if (context.world().isClient || stack.isEmpty()) return;
        if (((EntityLinkedItemStack) (Object) stack).apoli$getEntity() instanceof PlayerEntity player) {
            player.getItemCooldownManager().set(stack.getItem(), ticks);
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncItemActionTypes.COOLDOWN;
    }
}
