package dev.overgrown.sync.registry.utils;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.registry.Registry;

public class ApoliRegistryHelper {

    @SuppressWarnings("unchecked")
    public static <CT extends EntityConditionType> ConditionConfiguration<CT> registerEntityCondition(ConditionConfiguration<CT> configuration) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION_TYPE, configuration.id(), (ConditionConfiguration<EntityConditionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <CT extends BiEntityConditionType> ConditionConfiguration<CT> registerBientityCondition(ConditionConfiguration<CT> configuration) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION_TYPE, configuration.id(), (ConditionConfiguration<BiEntityConditionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <CT extends BlockConditionType> ConditionConfiguration<CT> registerBlockCondition(ConditionConfiguration<CT> configuration) {
        Registry.register(ApoliRegistries.BLOCK_CONDITION_TYPE, configuration.id(), (ConditionConfiguration<BlockConditionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <CT extends ItemConditionType> ConditionConfiguration<CT> registerItemCondition(ConditionConfiguration<CT> configuration) {
        Registry.register(ApoliRegistries.ITEM_CONDITION_TYPE, configuration.id(), (ConditionConfiguration<ItemConditionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <AT extends EntityActionType> ActionConfiguration<AT> registerEntityAction(ActionConfiguration<AT> configuration) {
        Registry.register(ApoliRegistries.ENTITY_ACTION_TYPE, configuration.id(), (ActionConfiguration<EntityActionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <AT extends BiEntityActionType> ActionConfiguration<AT> registerBientityAction(ActionConfiguration<AT> configuration) {
        Registry.register(ApoliRegistries.BIENTITY_ACTION_TYPE, configuration.id(), (ActionConfiguration<BiEntityActionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <AT extends ItemActionType> ActionConfiguration<AT> registerItemAction(ActionConfiguration<AT> configuration) {
        Registry.register(ApoliRegistries.ITEM_ACTION_TYPE, configuration.id(), (ActionConfiguration<ItemActionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <AT extends BlockActionType> ActionConfiguration<AT> registerBlockAction(ActionConfiguration<AT> configuration) {
        Registry.register(ApoliRegistries.BLOCK_ACTION_TYPE, configuration.id(), (ActionConfiguration<BlockActionType>) configuration);
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public static <PT extends PowerType> PowerConfiguration<PT> registerPower(PowerConfiguration<PT> configuration) {
        Registry.register(ApoliRegistries.POWER_TYPE, configuration.id(), (PowerConfiguration<PowerType>) configuration);
        return configuration;
    }
}
