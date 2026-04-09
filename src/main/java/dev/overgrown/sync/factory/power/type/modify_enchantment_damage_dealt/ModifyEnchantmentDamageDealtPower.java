package dev.overgrown.sync.factory.power.type.modify_enchantment_damage_dealt;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ModifyDamageDealtPower;
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

public class ModifyEnchantmentDamageDealtPower extends ModifyDamageDealtPower {

    private final Enchantment enchantment;
    private final float baseValue;
    // Store the modifiers that will be applied repeatedly
    private final List<Modifier> internalModifiers;

    public ModifyEnchantmentDamageDealtPower(PowerType<?> type, LivingEntity entity,
                                             Enchantment enchantment, float baseValue,
                                             Predicate<Pair<DamageSource, Float>> damageCondition,
                                             Predicate<Entity> targetCondition,
                                             Predicate<Pair<Entity, Entity>> biEntityCondition,
                                             Consumer<Pair<Entity, Entity>> biEntityAction,
                                             Modifier modifier, List<Modifier> modifiers) {
        super(type, entity, damageCondition, targetCondition, biEntityCondition);
        this.enchantment = enchantment;
        this.baseValue = baseValue;

        if (modifier != null) addModifier(modifier);
        if (modifiers != null) modifiers.forEach(this::addModifier);

        // Capture the list of modifiers for repeated application
        this.internalModifiers = super.getModifiers();

        if (biEntityAction != null) {
            setBiEntityAction(biEntityAction);
        }
    }

    /**
     * Computes the total damage bonus by applying the stored modifiers
     * (enchantment_level - 1) times to the base value.
     */
    private float computeBonus() {
        int level = EnchantmentHelper.getEquipmentLevel(enchantment, entity);
        if (level <= 0) return 0;

        float bonus = baseValue;
        for (int i = 0; i < level - 1; i++) {
            bonus = (float) ModifierUtil.applyModifiers(entity, internalModifiers, bonus);
        }
        return bonus;
    }

    @Override
    public List<Modifier> getModifiers() {
        // Return a single additive modifier that represents the total bonus
        float bonus = computeBonus();
        Modifier addModifier = ModifierUtil.createSimpleModifier(ModifierOperation.ADD_BASE_EARLY, bonus);
        return List.of(addModifier);
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("modify_enchantment_damage_dealt"),
                new SerializableData()
                        .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                        .add("base_value", SerializableDataTypes.FLOAT)
                        .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                        .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("modifier", Modifier.DATA_TYPE, null)
                        .add("modifiers", Modifier.LIST_TYPE, null)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
                data -> (powerType, livingEntity) -> {
                    // Provide a default true predicate for damage_condition if absent
                    Predicate<Pair<DamageSource, Float>> damageCondition =
                            data.isPresent("damage_condition")
                                    ? data.get("damage_condition")
                                    : dmg -> true;

                    ModifyEnchantmentDamageDealtPower power = new ModifyEnchantmentDamageDealtPower(
                            powerType,
                            livingEntity,
                            data.get("enchantment"),
                            data.getFloat("base_value"),
                            damageCondition,
                            data.get("target_condition"),
                            data.get("bientity_condition"),
                            data.get("bientity_action"),
                            data.get("modifier"),
                            data.get("modifiers")
                    );
                    return power;
                }
        ).allowCondition();
    }
}