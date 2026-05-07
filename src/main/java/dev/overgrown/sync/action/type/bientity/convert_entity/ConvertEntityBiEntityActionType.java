package dev.overgrown.sync.action.type.bientity.convert_entity;

import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

public class ConvertEntityBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<ConvertEntityBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("convert_to", SerializableDataTypes.ENTITY_TYPE)
            .add("ignore_difficulty", SerializableDataTypes.BOOLEAN, true),
        data -> new ConvertEntityBiEntityActionType(
            data.get("convert_to"),
            data.get("ignore_difficulty")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("convert_to", actionType.convertTo)
            .set("ignore_difficulty", actionType.ignoreDifficulty)
    );

    private final EntityType<?> convertTo;
    private final boolean ignoreDifficulty;

    public ConvertEntityBiEntityActionType(EntityType<?> convertTo, boolean ignoreDifficulty) {
        this.convertTo = convertTo;
        this.ignoreDifficulty = ignoreDifficulty;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (!(target instanceof MobEntity mobEntity)) return;
        if (!(target.getWorld() instanceof ServerWorld serverWorld)) return;

        if (!ignoreDifficulty) {
            Difficulty difficulty = serverWorld.getDifficulty();
            if (difficulty == Difficulty.EASY || difficulty == Difficulty.PEACEFUL) return;
            if (difficulty == Difficulty.NORMAL && serverWorld.getRandom().nextBoolean()) return;
        }

        if (mobEntity instanceof VillagerEntity villagerEntity && convertTo == EntityType.ZOMBIE_VILLAGER) {
            ZombieVillagerEntity zombieVillager = villagerEntity.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            if (zombieVillager != null) {
                zombieVillager.initialize(serverWorld, serverWorld.getLocalDifficulty(zombieVillager.getBlockPos()),
                    SpawnReason.CONVERSION, new ZombieEntity.ZombieData(false, true));
                zombieVillager.setVillagerData(villagerEntity.getVillagerData());
                zombieVillager.setXp(villagerEntity.getExperience());
                if (actor != null && !actor.isSilent()) {
                    serverWorld.syncWorldEvent(null, WorldEvents.ZOMBIE_INFECTS_VILLAGER, actor.getBlockPos(), 0);
                }
            }
            return;
        }

        MobEntity converted = mobEntity.convertTo((EntityType<? extends MobEntity>) convertTo, true);
        if (converted != null) {
            converted.playAmbientSound();
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.CONVERT_ENTITY;
    }
}
