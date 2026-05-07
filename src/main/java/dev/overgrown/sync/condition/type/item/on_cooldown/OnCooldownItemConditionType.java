package dev.overgrown.sync.condition.type.item.on_cooldown;

import dev.overgrown.sync.registry.SyncItemConditionTypes;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class OnCooldownItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<OnCooldownItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData(),
        data -> new OnCooldownItemConditionType(),
        (conditionType, serializableData) -> serializableData.instance()
    );

    @Override
    public boolean test(ItemConditionContext context) {
        if (context.stack().isEmpty()) return false;
        if (((EntityLinkedItemStack) (Object) context.stack()).apoli$getEntity() instanceof PlayerEntity player) {
            return player.getItemCooldownManager().isCoolingDown(context.stack().getItem());
        }
        return false;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncItemConditionTypes.ON_COOLDOWN;
    }
}
