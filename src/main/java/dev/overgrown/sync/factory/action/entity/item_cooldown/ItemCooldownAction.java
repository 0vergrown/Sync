package dev.overgrown.sync.factory.action.entity.item_cooldown;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.List;

public class ItemCooldownAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.getWorld().isClient || !(entity instanceof PlayerEntity player)) return;

        int ticks = data.getInt("ticks");

        if (data.isPresent("items")) {
            data.<List<Item>>get("items").forEach(item ->
                    player.getItemCooldownManager().set(item, ticks));
        }

        if (data.isPresent("item_tags")) {
            data.<List<TagKey<Item>>>get("item_tags").forEach(tagKey ->
                    Registries.ITEM.getEntryList(tagKey).ifPresent(entries ->
                            entries.forEach(entry ->
                                    player.getItemCooldownManager().set(entry.value(), ticks))));
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("item_cooldown"),
                new SerializableData()
                        .add("items", SerializableDataType.list(SerializableDataTypes.ITEM), null)
                        .add("item_tags", SerializableDataType.list(SerializableDataTypes.ITEM_TAG), null)
                        .add("ticks", SerializableDataTypes.INT, 20),
                ItemCooldownAction::action
        );
    }
}
