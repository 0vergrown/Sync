package dev.overgrown.sync.power.type.modify_enchantment_damage_taken;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.ModifyDamageTakenPowerType;
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

public class ModifyEnchantmentDamageTakenPowerType extends ModifyDamageTakenPowerType {

    public static final TypedDataObjectFactory<ModifyEnchantmentDamageTakenPowerType> DATA_FACTORY = createConditionedModifyingRequiredDataFactory(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("base_value", SerializableDataTypes.FLOAT)
            .add("self_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("attacker_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifyEnchantmentDamageTakenPowerType(
            data.get("enchantment"),
            data.get("base_value"),
            data.get("self_action"),
            data.get("attacker_action"),
            data.get("bientity_action"),
            data.get("bientity_condition"),
            data.get("damage_condition"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("enchantment", powerType.enchantment)
            .set("base_value", powerType.baseValue)
            .set("self_action", powerType.optSelfAction)
            .set("attacker_action", powerType.optAttackerAction)
            .set("bientity_action", powerType.optBiEntityAction)
            .set("bientity_condition", powerType.optBiEntityCondition)
            .set("damage_condition", powerType.optDamageCondition)
    );

    private final RegistryKey<Enchantment> enchantment;
    private final float baseValue;

    private final Optional<EntityAction> optSelfAction;
    private final Optional<EntityAction> optAttackerAction;
    private final Optional<BiEntityAction> optBiEntityAction;
    private final Optional<BiEntityCondition> optBiEntityCondition;
    private final Optional<DamageCondition> optDamageCondition;

    private LivingEntity currentAttacker;

    public ModifyEnchantmentDamageTakenPowerType(RegistryKey<Enchantment> enchantment, float baseValue,
                                                 Optional<EntityAction> selfAction,
                                                 Optional<EntityAction> attackerAction,
                                                 Optional<BiEntityAction> biEntityAction,
                                                 Optional<BiEntityCondition> biEntityCondition,
                                                 Optional<DamageCondition> damageCondition,
                                                 List<Modifier> modifiers,
                                                 Optional<EntityCondition> condition) {
        super(selfAction, attackerAction, biEntityAction, Optional.empty(), Optional.empty(),
              biEntityCondition, damageCondition, modifiers, condition);
        this.enchantment = enchantment;
        this.baseValue = baseValue;
        this.optSelfAction = selfAction;
        this.optAttackerAction = attackerAction;
        this.optBiEntityAction = biEntityAction;
        this.optBiEntityCondition = biEntityCondition;
        this.optDamageCondition = damageCondition;
    }

    @Override
    public boolean doesApply(DamageSource source, float damageAmount) {
        currentAttacker = (source.getAttacker() instanceof LivingEntity le) ? le : null;
        return super.doesApply(source, damageAmount);
    }

    private float computeBonus() {
        if (currentAttacker == null) return 0f;
        RegistryEntry.Reference<Enchantment> entry = currentAttacker.getWorld().getRegistryManager()
            .get(RegistryKeys.ENCHANTMENT)
            .entryOf(enchantment);
        int level = EnchantmentHelper.getEquipmentLevel(entry, currentAttacker);
        if (level <= 0) return 0f;

        float bonus = baseValue;
        List<Modifier> baseModifiers = super.getModifiers();
        for (int i = 0; i < level - 1; i++) {
            bonus = (float) ModifierUtil.applyModifiers(currentAttacker, baseModifiers, bonus);
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

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.MODIFY_ENCHANTMENT_DAMAGE_TAKEN;
    }
}
