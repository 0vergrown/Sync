package dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils;

import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.List;

public class BodyPartModifierEntry {

    private final BodyRegion region;
    private final List<Modifier> modifiers;

    public BodyPartModifierEntry(BodyRegion region, List<Modifier> modifiers) {
        this.region = region;
        this.modifiers = modifiers;
    }

    public BodyRegion getRegion() {
        return region;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    // ---- Serialisation ----
    public static final SerializableDataType<BodyPartModifierEntry> DATA_TYPE =
            SerializableDataType.compound(
                    BodyPartModifierEntry.class,
                    new SerializableData()
                            .add("body_part", SerializableDataTypes.STRING)          // "head", "torso", …, "any"
                            .add("modifier",  Modifier.DATA_TYPE,      null)
                            .add("modifiers", Modifier.LIST_TYPE,      null),
                    data -> {
                        String partName = data.getString("body_part").toUpperCase();
                        BodyRegion region;
                        try {
                            region = BodyRegion.valueOf(partName);
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(
                                    "Unknown body_part \"" + data.getString("body_part") + "\". " +
                                            "Valid values: head, torso, left_arm, right_arm, legs, feet, any");
                        }

                        java.util.List<Modifier> mods = new java.util.ArrayList<>();
                        data.<Modifier>ifPresent("modifier", mods::add);
                        data.<List<Modifier>>ifPresent("modifiers", mods::addAll);
                        return new BodyPartModifierEntry(region, mods);
                    },
                    (data, entry) -> {
                        SerializableData.Instance inst = data.new Instance();
                        inst.set("body_part", entry.getRegion().name().toLowerCase());
                        inst.set("modifier",  null);
                        inst.set("modifiers", entry.getModifiers());
                        return inst;
                    }
            );

    public static final SerializableDataType<List<BodyPartModifierEntry>> LIST_TYPE =
            SerializableDataType.list(DATA_TYPE);
}