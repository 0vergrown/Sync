package dev.overgrown.sync.power.type.modify_enchantment_damage_dealt;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.ModifyDamageDealtPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModifyEnchantmentDamageDealtPowerType extends ModifyDamageDealtPowerType {

    public static final TypedDataObjectFactory<ModifyEnchantmentDamageDealtPowerType> DATA_FACTORY = createConditionedModifyingRequiredDataFactory(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("base_value", SerializableDataTypes.FLOAT)
            .add("self_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("target_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("target_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifyEnchantmentDamageDealtPowerType(
            data.get("enchantment"),
            data.get("base_value"),
            data.get("self_action"),
            data.get("target_action"),
            data.get("bientity_action"),
            data.get("target_condition"),
            data.get("bientity_condition"),
            data.get("damage_condition"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("enchantment", powerType.enchantment)
            .set("base_value", powerType.baseValue)
            .set("self_action", powerType.getOptionalSelfAction())
            .set("target_action", powerType.getOptionalTargetAction())
            .set("bientity_action", powerType.getOptionalBiEntityAction())
            .set("target_condition", powerType.getOptionalTargetCondition())
            .set("bientity_condition", powerType.getOptionalBiEntityCondition())
            .set("damage_condition", powerType.getOptionalDamageCondition())
    );

    private final RegistryKey<Enchantment> enchantment;
    private final float baseValue;

    private final Optional<EntityAction> selfAction;
    private final Optional<EntityAction> targetAction;
    private final Optional<BiEntityAction> biEntityAction;
    private final Optional<EntityCondition> targetCondition;
    private final Optional<BiEntityCondition> biEntityCondition;
    private final Optional<DamageCondition> damageCondition;

    public ModifyEnchantmentDamageDealtPowerType(RegistryKey<Enchantment> enchantment, float baseValue,
                                                 Optional<EntityAction> selfAction,
                                                 Optional<EntityAction> targetAction,
                                                 Optional<BiEntityAction> biEntityAction,
                                                 Optional<EntityCondition> targetCondition,
                                                 Optional<BiEntityCondition> biEntityCondition,
                                                 Optional<DamageCondition> damageCondition,
                                                 List<Modifier> modifiers,
                                                 Optional<EntityCondition> condition) {
        super(selfAction, targetAction, biEntityAction, targetCondition, biEntityCondition, damageCondition, modifiers, condition);
        this.enchantment = enchantment;
        this.baseValue = baseValue;
        this.selfAction = selfAction;
        this.targetAction = targetAction;
        this.biEntityAction = biEntityAction;
        this.targetCondition = targetCondition;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;
    }

    private float computeBonus() {
        if (!(getHolder() instanceof LivingEntity livingEntity)) return 0;
        RegistryEntry.Reference<Enchantment> entry = livingEntity.getWorld().getRegistryManager()
            .get(RegistryKeys.ENCHANTMENT)
            .entryOf(enchantment);
        int level = EnchantmentHelper.getEquipmentLevel(entry, livingEntity);
        if (level <= 0) return 0;

        float bonus = baseValue;
        List<Modifier> baseModifiers = super.getModifiers();
        for (int i = 0; i < level - 1; i++) {
            bonus = (float) ModifierUtil.applyModifiers(getHolder(), baseModifiers, bonus);
        }
        return bonus;
    }

    @Override
    public List<Modifier> getModifiers() {
        float bonus = computeBonus();
        Modifier addModifier = ModifierUtil.createSimpleModifier(ModifierOperation.ADD_BASE_EARLY, bonus);
        List<Modifier> result = new ArrayList<>();
        result.add(addModifier);
        return result;
    }

    public Optional<EntityAction> getOptionalSelfAction() { return selfAction; }
    public Optional<EntityAction> getOptionalTargetAction() { return targetAction; }
    public Optional<BiEntityAction> getOptionalBiEntityAction() { return biEntityAction; }
    public Optional<EntityCondition> getOptionalTargetCondition() { return targetCondition; }
    public Optional<BiEntityCondition> getOptionalBiEntityCondition() { return biEntityCondition; }
    public Optional<DamageCondition> getOptionalDamageCondition() { return damageCondition; }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.MODIFY_ENCHANTMENT_DAMAGE_DEALT;
    }
}
