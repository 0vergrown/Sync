package dev.overgrown.sync.factory.compatibility.aspectslib.condition;

import dev.overgrown.aspectslib.api.IAspectAffinityEntity;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.List;

public class HasAspectCondition {

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("has_aspect"),
                new SerializableData()
                        .add("aspect", SerializableDataTypes.IDENTIFIER, null)
                        .add("aspects", SerializableDataTypes.IDENTIFIERS, null)
                        .add("min", SerializableDataTypes.INT, 1)
                        .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL),
                (data, entity) -> {
                    if (!(entity instanceof IAspectAffinityEntity)) {
                        return false;
                    }

                    IAspectAffinityEntity aspectEntity = (IAspectAffinityEntity) entity;
                    AspectData aspectData = aspectEntity.aspectslib$getAspectData();
                    Comparison comparison = data.get("comparison");
                    int minLevel = data.getInt("min");

                    if (data.isPresent("aspect")) {
                        Identifier aspectId = data.getId("aspect");
                        int level = aspectData.getLevel(aspectId);
                        return comparison.compare(level, minLevel);
                    }
                    else if (data.isPresent("aspects")) {
                        List<Identifier> aspectIds = data.get("aspects");
                        for (Identifier aspectId : aspectIds) {
                            int level = aspectData.getLevel(aspectId);
                            if (comparison.compare(level, minLevel)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return false;
                }
        );
    }
}