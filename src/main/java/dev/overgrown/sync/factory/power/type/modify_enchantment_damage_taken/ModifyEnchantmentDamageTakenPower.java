package dev.overgrown.sync.factory.power.type.modify_enchantment_damage_taken;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ModifyDamageTakenPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyEnchantmentDamageTakenPower extends ModifyDamageTakenPower {

    private final Enchantment enchantment;
    private final float baseValue;

    /**
     * Snapshot of the modifiers captured at construction time so we can apply
     * them repeatedly in {@link #computeBonus()} without re-querying the super
     * list on every hit.
     */
    private final List<Modifier> internalModifiers;

    /**
     * Populated inside {@link #doesApply} so that {@link #computeBonus()} can
     * look up the correct attacker's equipment without an extra argument.
     * This is safe because Apoli always evaluates {@code doesApply} and
     * {@code getModifiers} sequentially on the same thread.
     */
    private LivingEntity currentAttacker = null;

    public ModifyEnchantmentDamageTakenPower(PowerType<?> type,
                                             LivingEntity entity,
                                             Enchantment enchantment,
                                             float baseValue,
                                             Predicate<Pair<DamageSource, Float>> damageCondition,
                                             Predicate<Pair<Entity, Entity>> biEntityCondition,
                                             Consumer<Pair<Entity, Entity>> biEntityAction,
                                             Modifier modifier,
                                             List<Modifier> modifiers) {
        super(type, entity, damageCondition, biEntityCondition);
        this.enchantment = enchantment;
        this.baseValue = baseValue;

        if (modifier != null) addModifier(modifier);
        if (modifiers != null) modifiers.forEach(this::addModifier);

        // Capture modifier list after all entries have been added.
        this.internalModifiers = super.getModifiers();

        if (biEntityAction != null) {
            setBiEntityAction(biEntityAction);
        }
    }

    /**
     * Extends the parent check by also capturing the attacker reference so
     * {@link #computeBonus()} can look up their equipment level.
     */
    @Override
    public boolean doesApply(DamageSource source, float damageAmount) {
        currentAttacker = (source.getAttacker() instanceof LivingEntity le) ? le : null;
        return super.doesApply(source, damageAmount);
    }

    /**
     * Computes the flat damage bonus contributed by this power for the current
     * hit.  Returns {@code 0} when there is no living attacker or the attacker
     * does not have at least one level of the target enchantment.
     */
    private float computeBonus() {
        if (currentAttacker == null) return 0f;

        int level = EnchantmentHelper.getEquipmentLevel(enchantment, currentAttacker);
        if (level <= 0) return 0f;

        float bonus = baseValue;
        // Apply modifier(s) once for every level above 1.
        for (int i = 0; i < level - 1; i++) {
            bonus = (float) ModifierUtil.applyModifiers(currentAttacker, internalModifiers, bonus);
        }
        return bonus;
    }

    /**
     * Returns a single additive modifier representing the total bonus for
     * this hit, computed from the attacker's enchantment level.
     */
    @Override
    public List<Modifier> getModifiers() {
        float bonus = computeBonus();
        Modifier addModifier = ModifierUtil.createSimpleModifier(ModifierOperation.ADD_BASE_EARLY, bonus);
        return List.of(addModifier);
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("modify_enchantment_damage_taken"),
                new SerializableData()
                        .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                        .add("base_value", SerializableDataTypes.FLOAT)
                        .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("modifier", Modifier.DATA_TYPE, null)
                        .add("modifiers", Modifier.LIST_TYPE, null)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
                data -> (powerType, livingEntity) -> {
                    // Default damage condition: accept all damage
                    Predicate<Pair<DamageSource, Float>> damageCondition =
                            data.isPresent("damage_condition")
                                    ? data.get("damage_condition")
                                    : dmg -> true;

                    return new ModifyEnchantmentDamageTakenPower(
                            powerType,
                            livingEntity,
                            data.get("enchantment"),
                            data.getFloat("base_value"),
                            damageCondition,
                            data.get("bientity_condition"),
                            data.get("bientity_action"),
                            data.get("modifier"),
                            data.get("modifiers")
                    );
                }
        ).allowCondition();
    }
}