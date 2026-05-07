package dev.overgrown.sync.power.type.body_part_damage_modifier.util;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BodyPartModifierEntry {

    private final BodyRegion region;
    private final List<Modifier> modifiers;
    private final Optional<BiEntityAction> bientityAction;

    public BodyPartModifierEntry(BodyRegion region,
                                 List<Modifier> modifiers,
                                 Optional<BiEntityAction> bientityAction) {
        this.region = region;
        this.modifiers = modifiers;
        this.bientityAction = bientityAction;
    }

    public BodyRegion getRegion() { return region; }
    public List<Modifier> getModifiers() { return modifiers; }
    public Optional<BiEntityAction> getBientityAction() { return bientityAction; }

    public static final SerializableDataType<BodyPartModifierEntry> DATA_TYPE = SerializableDataType.compound(
        new SerializableData()
            .add("body_part", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("x_min", SerializableDataTypes.DOUBLE, -1.0)
            .add("x_max", SerializableDataTypes.DOUBLE, 1.0)
            .add("y_min", SerializableDataTypes.DOUBLE, 0.0)
            .add("y_max", SerializableDataTypes.DOUBLE, 1.0)
            .add("z_min", SerializableDataTypes.DOUBLE, -1.0)
            .add("z_max", SerializableDataTypes.DOUBLE, 1.0)
            .add("modifier", Modifier.DATA_TYPE.optional(), Optional.empty())
            .add("modifiers", Modifier.LIST_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> {
            BodyRegion region;
            Optional<String> presetName = data.get("body_part");

            if (presetName.isPresent()) {
                region = BodyRegion.fromPresetName(presetName.get());
                if (region == null) {
                    throw new IllegalArgumentException(
                        "Unknown body_part preset \"" + presetName.get() + "\". Use a preset name " +
                        "(head, torso, left_arm, right_arm, legs, feet, chest, back, achilles_heel, any) " +
                        "or omit body_part and specify x_min/x_max/y_min/y_max/z_min/z_max.");
                }
            } else {
                region = new BodyRegion(
                    data.get("x_min"), data.get("x_max"),
                    data.get("y_min"), data.get("y_max"),
                    data.get("z_min"), data.get("z_max")
                );
            }

            List<Modifier> mods = new ArrayList<>();
            data.<Optional<Modifier>>get("modifier").ifPresent(mods::add);
            data.<Optional<List<Modifier>>>get("modifiers").ifPresent(mods::addAll);

            return new BodyPartModifierEntry(region, mods, data.get("bientity_action"));
        },
        (entry, sd) -> sd.instance()
            .set("body_part", Optional.<String>empty())
            .set("x_min", entry.region.minX)
            .set("x_max", entry.region.maxX)
            .set("y_min", entry.region.minY)
            .set("y_max", entry.region.maxY)
            .set("z_min", entry.region.minZ)
            .set("z_max", entry.region.maxZ)
            .set("modifier", Optional.<Modifier>empty())
            .set("modifiers", Optional.of(entry.modifiers))
            .set("bientity_action", entry.bientityAction)
    );

    public static final SerializableDataType<List<BodyPartModifierEntry>> LIST_TYPE = DATA_TYPE.list();
}
