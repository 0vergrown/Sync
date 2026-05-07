package dev.overgrown.sync.condition.type.entity.mod_loaded;

import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModLoadedEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<ModLoadedEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("mod", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("mods", SerializableDataTypes.STRINGS.optional(), Optional.empty()),
        data -> new ModLoadedEntityConditionType(
            data.get("mod"),
            data.get("mods")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("mod", conditionType.mod)
            .set("mods", conditionType.mods)
    );

    private final Optional<String> mod;
    private final Optional<List<String>> mods;

    public ModLoadedEntityConditionType(Optional<String> mod, Optional<List<String>> mods) {
        this.mod = mod;
        this.mods = mods;
    }

    public static boolean check(Optional<String> mod, Optional<List<String>> mods) {
        FabricLoader loader = FabricLoader.getInstance();

        if (mod.isPresent() && !loader.isModLoaded(mod.get())) {
            return false;
        }

        if (mods.isPresent()) {
            for (String m : mods.get()) {
                if (!loader.isModLoaded(m)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        return check(mod, mods);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.MOD_LOADED;
    }
}
