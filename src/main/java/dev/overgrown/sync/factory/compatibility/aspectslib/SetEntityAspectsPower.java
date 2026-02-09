package dev.overgrown.sync.factory.compatibility.aspectslib;

import dev.overgrown.aspectslib.api.IAspectAffinityEntity;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.compatibility.aspectslib.data.DataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class SetEntityAspectsPower extends Power {
    private final AspectData aspectData;
    private AspectData originalAspectData;

    public SetEntityAspectsPower(PowerType<?> type, LivingEntity entity, Map<String, Integer> aspects) {
        super(type, entity);
        this.aspectData = convertToAspectData(aspects);
    }

    @Override
    public void onGained() {
        if (entity instanceof IAspectAffinityEntity aspectAffinity) {
            // Save original aspects
            originalAspectData = aspectAffinity.aspectslib$getAspectData();

            // Overwrite with new aspects
            aspectAffinity.aspectslib$setAspectData(aspectData);
        }
    }

    @Override
    public void onLost() {
        if (entity instanceof IAspectAffinityEntity aspectAffinity) {
            // Restore original aspects
            aspectAffinity.aspectslib$setAspectData(originalAspectData);
        }
    }

    private AspectData convertToAspectData(Map<String, Integer> aspects) {
        Object2IntOpenHashMap<Identifier> aspectMap = new Object2IntOpenHashMap<>();
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            Identifier aspectId = new Identifier(entry.getKey());
            aspectMap.put(aspectId, entry.getValue().intValue());
        }
        return new AspectData(aspectMap);
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                Sync.identifier("set_entity_aspects"),
                new SerializableData()
                        .add("aspects", DataTypes.STRING_INT_MAP),
                data -> (type, entity) -> new SetEntityAspectsPower(type, entity, data.get("aspects"))
        ).allowCondition();
    }
}