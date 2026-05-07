package dev.overgrown.sync.action.type.entity.item_cooldown;

import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ItemCooldownEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ItemCooldownEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("items", SerializableDataTypes.ITEM.list().optional(), Optional.empty())
            .add("item_tags", SerializableDataTypes.ITEM_TAG.list().optional(), Optional.empty())
            .add("ticks", SerializableDataTypes.INT, 20),
        data -> new ItemCooldownEntityActionType(
            data.get("items"),
            data.get("item_tags"),
            data.get("ticks")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("items", actionType.items)
            .set("item_tags", actionType.itemTags)
            .set("ticks", actionType.ticks)
    );

    private final Optional<List<Item>> items;
    private final Optional<List<TagKey<Item>>> itemTags;
    private final int ticks;

    public ItemCooldownEntityActionType(Optional<List<Item>> items, Optional<List<TagKey<Item>>> itemTags, int ticks) {
        this.items = items;
        this.itemTags = itemTags;
        this.ticks = ticks;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity().getWorld().isClient || !(context.entity() instanceof PlayerEntity player)) return;

        items.ifPresent(list -> list.forEach(item -> player.getItemCooldownManager().set(item, ticks)));

        itemTags.ifPresent(tags -> tags.forEach(tagKey ->
            Registries.ITEM.getEntryList(tagKey).ifPresent(entries ->
                entries.forEach(entry -> player.getItemCooldownManager().set(entry.value(), ticks))
            )
        ));
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.ITEM_COOLDOWN;
    }
}
