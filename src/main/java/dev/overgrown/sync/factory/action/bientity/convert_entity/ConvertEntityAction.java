package dev.overgrown.sync.factory.action.bientity.convert_entity;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldEvents;

public class ConvertEntityAction {

    @SuppressWarnings("unchecked")
    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if (!(target instanceof MobEntity mobEntity)) return;
        if (!(target.getWorld() instanceof ServerWorld serverWorld)) return;

        if (!data.getBoolean("ignore_difficulty")) {
            Difficulty difficulty = serverWorld.getDifficulty();
            if (difficulty == Difficulty.EASY || difficulty == Difficulty.PEACEFUL) return;
            if (difficulty == Difficulty.NORMAL && serverWorld.getRandom().nextBoolean()) return;
        }

        EntityType<?> newEntityType = data.get("convert_to");

        // Villager -> ZombieVillager needs special handling to preserve trades, gossip, XP
        if (mobEntity instanceof VillagerEntity villagerEntity && newEntityType == EntityType.ZOMBIE_VILLAGER) {
            ZombieVillagerEntity zombieVillager = villagerEntity.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            if (zombieVillager != null) {
                zombieVillager.initialize(serverWorld, serverWorld.getLocalDifficulty(zombieVillager.getBlockPos()),
                        SpawnReason.CONVERSION, new ZombieEntity.ZombieData(false, true), null);
                zombieVillager.setVillagerData(villagerEntity.getVillagerData());
                zombieVillager.setGossipData(villagerEntity.getGossip().serialize(NbtOps.INSTANCE));
                zombieVillager.setOfferData(villagerEntity.getOffers().toNbt());
                zombieVillager.setXp(villagerEntity.getExperience());
                if (!actor.isSilent()) {
                    serverWorld.syncWorldEvent(null, WorldEvents.ZOMBIE_INFECTS_VILLAGER, actor.getBlockPos(), 0);
                }
            }
            return;
        }

        MobEntity converted = mobEntity.convertTo((EntityType<? extends MobEntity>) newEntityType, true);
        if (converted != null) {
            converted.playAmbientSound();
        }
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("convert_entity"),
                new SerializableData()
                        .add("convert_to", SerializableDataTypes.ENTITY_TYPE)
                        .add("ignore_difficulty", SerializableDataTypes.BOOLEAN, true),
                ConvertEntityAction::action
        );
    }
}