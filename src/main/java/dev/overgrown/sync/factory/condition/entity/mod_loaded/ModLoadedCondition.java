package dev.overgrown.sync.factory.condition.entity.mod_loaded;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;

import java.util.List;

public class ModLoadedCondition {

    public static SerializableData dataSchema() {
        return new SerializableData()
                .add("mod", SerializableDataTypes.STRING, null)
                .add("mods", SerializableDataTypes.STRINGS, null);
    }

    public static boolean check(SerializableData.Instance data) {
        FabricLoader loader = FabricLoader.getInstance();

        if (data.isPresent("mod") && !loader.isModLoaded(data.getString("mod"))) {
            return false;
        }

        if (data.isPresent("mods")) {
            List<String> mods = data.get("mods");
            for (String mod : mods) {
                if (!loader.isModLoaded(mod)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("mod_loaded"),
                dataSchema(),
                (data, entity) -> check(data)
        );
    }
}
