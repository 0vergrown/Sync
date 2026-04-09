package dev.overgrown.sync.factory.compatibility.jade.power.type.modify_jade_tooltip;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.Prioritized;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

/**
 * An Apoli power that modifies how this entity appears in Jade's tooltip
 * when viewed by another player/entity.
 *
 * <p>JSON fields:</p>
 * <ul>
 *   <li>{@code hide_tooltip}: Hide the entire Jade tooltip (default: false)</li>
 *   <li>{@code display_name}: Override the entity's name line (optional Text)</li>
 *   <li>{@code hide_health}: Hide the health element (default: false)</li>
 *   <li>{@code hide_armor}: Hide the armor element (default: false)</li>
 *   <li>{@code hide_potion_effects}: Hide the potion effects element (default: false)</li>
 *   <li>{@code priority}: Higher value wins when multiple powers are active (default: 0)</li>
 *   <li>{@code entity_condition}: Entity condition tested against the <em>viewer</em></li>
 *   <li>{@code bientity_condition}: Bi-entity condition with actor = viewer, target = power holder</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * {
 *   "type": "sync:modify_jade_tooltip",
 *   "hide_health": true,
 *   "display_name": {"text": "???", "color": "dark_red"},
 *   "bientity_condition": {
 *     "type": "apoli:distance",
 *     "comparison": ">=",
 *     "compare_to": 10
 *   }
 * }
 * }</pre>
 */
public class ModifyJadeTooltipPower extends Power implements Prioritized<ModifyJadeTooltipPower> {

    private final boolean hideTooltip;
    private final Text displayName;
    private final boolean hideHealth;
    private final boolean hideArmor;
    private final boolean hidePotionEffects;
    private final int priority;
    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public ModifyJadeTooltipPower(PowerType<?> type,
                                  LivingEntity entity,
                                  boolean hideTooltip,
                                  Text displayName,
                                  boolean hideHealth,
                                  boolean hideArmor,
                                  boolean hidePotionEffects,
                                  int priority,
                                  Predicate<Entity> entityCondition,
                                  Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.hideTooltip = hideTooltip;
        this.displayName = displayName;
        this.hideHealth = hideHealth;
        this.hideArmor = hideArmor;
        this.hidePotionEffects = hidePotionEffects;
        this.priority = priority;
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
    }

    // -----------------------------------------------------------------------
    // Viewer check (mirrors ModifyLabelRenderPower#shouldApplyForViewer)
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if this power should take effect for the given viewer.
     * Checks both {@code entity_condition} (on viewer) and {@code bientity_condition}
     * (actor = viewer, target = holder).
     */
    public boolean shouldApplyForViewer(Entity viewer) {
        if (viewer == null) return true;
        if (entityCondition   != null && !entityCondition.test(viewer)) return false;
        if (bientityCondition != null && !bientityCondition.test(new Pair<>(viewer, this.entity))) return false;
        return true;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public boolean isHideTooltip() {
        return hideTooltip;
    }
    public Text    getDisplayName() {
        return displayName;
    }
    public boolean isHideHealth() {
        return hideHealth;
    }
    public boolean isHideArmor() {
        return hideArmor;
    }
    public boolean isHidePotionEffects() {
        return hidePotionEffects;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    // -----------------------------------------------------------------------
    // Factory
    // -----------------------------------------------------------------------

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("modify_jade_tooltip"),
                new SerializableData()
                        .add("hide_tooltip", SerializableDataTypes.BOOLEAN,false)
                        .add("display_name", SerializableDataTypes.TEXT,null)
                        .add("hide_health", SerializableDataTypes.BOOLEAN,false)
                        .add("hide_armor", SerializableDataTypes.BOOLEAN,false)
                        .add("hide_potion_effects", SerializableDataTypes.BOOLEAN,false)
                        .add("priority", SerializableDataTypes.INT,0)
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION,null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, player) -> new ModifyJadeTooltipPower(
                        type,
                        player,
                        data.getBoolean("hide_tooltip"),
                        data.get("display_name"),
                        data.getBoolean("hide_health"),
                        data.getBoolean("hide_armor"),
                        data.getBoolean("hide_potion_effects"),
                        data.getInt("priority"),
                        data.get("entity_condition"),
                        data.get("bientity_condition")
                )
        ).allowCondition();
    }
}