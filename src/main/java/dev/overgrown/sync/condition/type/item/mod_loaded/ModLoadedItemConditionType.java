package dev.overgrown.sync.condition.type.item.mod_loaded;

import dev.overgrown.sync.condition.type.entity.mod_loaded.ModLoadedEntityConditionType;
import dev.overgrown.sync.registry.SyncItemConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModLoadedItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<ModLoadedItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("mod", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("mods", SerializableDataTypes.STRINGS.optional(), Optional.empty()),
        data -> new ModLoadedItemConditionType(data.get("mod"), data.get("mods")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("mod", conditionType.mod)
            .set("mods", conditionType.mods)
    );

    private final Optional<String> mod;
    private final Optional<List<String>> mods;

    public ModLoadedItemConditionType(Optional<String> mod, Optional<List<String>> mods) {
        this.mod = mod;
        this.mods = mods;
    }

    @Override
    public boolean test(ItemConditionContext context) {
        return ModLoadedEntityConditionType.check(mod, mods);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncItemConditionTypes.MOD_LOADED;
    }
}
