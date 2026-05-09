package dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BodyPartModifierEntry {

    private final BodyRegion region;
    private final List<Modifier> modifiers;
    @Nullable
    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public BodyPartModifierEntry(BodyRegion region,
                                 List<Modifier> modifiers,
                                 @Nullable Consumer<Pair<Entity, Entity>> bientityAction) {
        this.region = region;
        this.modifiers = modifiers;
        this.bientityAction = bientityAction;
    }

    public BodyRegion getRegion() {
        return region;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    @Nullable
    public Consumer<Pair<Entity, Entity>> getBientityAction() {
        return bientityAction;
    }

    // Serialisation
    public static final SerializableDataType<BodyPartModifierEntry> DATA_TYPE =
            SerializableDataType.compound(
                    BodyPartModifierEntry.class,
                    new SerializableData()
                            // Named Preset (optional)
                            .add("body_part", SerializableDataTypes.STRING, null)
                            // Explicit Ranges (optional, override preset)
                            .add("x_min", SerializableDataTypes.DOUBLE, -1.0)
                            .add("x_max", SerializableDataTypes.DOUBLE, 1.0)
                            .add("y_min", SerializableDataTypes.DOUBLE, 0.0)
                            .add("y_max", SerializableDataTypes.DOUBLE, 1.0)
                            .add("z_min", SerializableDataTypes.DOUBLE, -1.0)
                            .add("z_max", SerializableDataTypes.DOUBLE, 1.0)
                            // Modifiers
                            .add("modifier",  Modifier.DATA_TYPE, null)
                            .add("modifiers", Modifier.LIST_TYPE, null)
                            // Bi-entity action (optional, fires with attacker -> target)
                            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
                    data -> {
                        BodyRegion region;

                        String presetName = data.isPresent("body_part")
                                ? data.getString("body_part") : null;

                        if (presetName != null) {
                            region = BodyRegion.fromPresetName(presetName);
                            if (region == null) {
                                throw new IllegalArgumentException(
                                        "Unknown body_part preset \"" + presetName + "\". " +
                                                "Use a preset name (head, torso, left_arm, right_arm, " +
                                                "legs, feet, chest, back, achilles_heel, any) or omit " +
                                                "body_part and specify x_min/x_max/y_min/y_max/z_min/z_max.");
                            }
                        } else {
                            // Fully explicit range definition
                            region = new BodyRegion(
                                    data.getDouble("x_min"), data.getDouble("x_max"),
                                    data.getDouble("y_min"), data.getDouble("y_max"),
                                    data.getDouble("z_min"), data.getDouble("z_max")
                            );
                        }

                        List<Modifier> mods = new ArrayList<>();
                        data.<Modifier>ifPresent("modifier",  mods::add);
                        data.<List<Modifier>>ifPresent("modifiers", mods::addAll);

                        Consumer<Pair<Entity, Entity>> action = data.isPresent("bientity_action")
                                ? data.get("bientity_action") : null;

                        return new BodyPartModifierEntry(region, mods, action);
                    },
                    (data, entry) -> {
                        SerializableData.Instance inst = data.new Instance();
                        inst.set("body_part", null);
                        inst.set("x_min", entry.region.minX);
                        inst.set("x_max", entry.region.maxX);
                        inst.set("y_min", entry.region.minY);
                        inst.set("y_max", entry.region.maxY);
                        inst.set("z_min", entry.region.minZ);
                        inst.set("z_max", entry.region.maxZ);
                        inst.set("modifier",  null);
                        inst.set("modifiers", entry.modifiers);
                        inst.set("bientity_action", entry.bientityAction);
                        return inst;
                    }
            );

    public static final SerializableDataType<List<BodyPartModifierEntry>> LIST_TYPE =
            SerializableDataType.list(DATA_TYPE);
}