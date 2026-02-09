package dev.overgrown.sync.factory.action.entity.summons;

import java.util.function.Consumer;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;

public class SummonCloneAction {
    public static void action (SerializableData.Instance data, Entity entity) {
        if (entity instanceof PlayerEntity player) {
            final boolean canSit = data.getBoolean("can_sit");
            final boolean canAttack = data.getBoolean("can_attack");
            final boolean followOwner = data.getBoolean("follow_owner");
            final boolean inheritsEquipment = data.getBoolean("inherit_equipment");
            final boolean inheritsEnchantments = data.getBoolean("inherit_enchantments");
            final Consumer<Pair<Entity, Entity>> bientityAction = data.get("bientity_action");

            CloneEntity clone = summon(player, canSit, followOwner, canAttack, inheritsEquipment, inheritsEnchantments);
            if (bientityAction != null && clone != null) bientityAction.accept(new Pair<>(player, clone));
        } else {
            Sync.LOGGER.warn("Attempted to summon clone of invalid entity. Only Players are compatible with this action type.");
        }
    }

    private static CloneEntity summon (PlayerEntity player, boolean canSit, boolean followOwner, boolean canAttack, boolean inheritsEquipment, boolean inheritsEnchantments) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            CloneEntity clone = new CloneEntity(SyncEntityRegistry.CLONE, serverWorld);
            clone.setCanSit(canSit);
            clone.setCanAttack(canAttack);
            clone.setFollowOwner(followOwner);

            clone.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getHeadYaw(), 0);

            clone.initialize(serverWorld, serverWorld.getLocalDifficulty(player.getBlockPos()), SpawnReason.REINFORCEMENT, null, null);
            clone.setCustomName(player.getName());
            clone.setOwnerUUID(player.getUuid());
            clone.setCanPickUpLoot(false);
            clone.setPersistent();

            serverWorld.spawnEntity(clone);

            if (inheritsEquipment) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (!player.getEquippedStack(slot).isEmpty()) {
                        clone.setEquipmentDropChance(slot, 0f);

                        ItemStack copy = new ItemStack(player.getEquippedStack(slot).getItem(), player.getEquippedStack(slot).getCount());
                        if (inheritsEnchantments) {
                            EnchantmentHelper.set(EnchantmentHelper.get(player.getEquippedStack(slot)), copy);
                        }
                        clone.equipStack(slot, copy);
                    }
                }
            }
            clone.updateWeaponGoals();
            return clone;
        }
        return null;
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Sync.identifier("summon_clone"),
                new SerializableData()
                        .add("can_sit", SerializableDataTypes.BOOLEAN, true)
                        .add("can_attack", SerializableDataTypes.BOOLEAN, true)
                        .add("follow_owner", SerializableDataTypes.BOOLEAN, true)
                        .add("inherit_equipment", SerializableDataTypes.BOOLEAN, true)
                        .add("inherit_enchantments", SerializableDataTypes.BOOLEAN, true)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
                SummonCloneAction::action
        );
    }
}