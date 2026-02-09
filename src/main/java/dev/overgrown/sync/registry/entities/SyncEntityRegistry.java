package dev.overgrown.sync.registry.entities;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.factory.power.type.custom_projectile.entities.CustomProjectileEntity;
import dev.overgrown.sync.factory.action.entity.summons.entities.minion.MinionEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SyncEntityRegistry {
    public static final EntityType<CloneEntity> CLONE = register(
            Sync.identifier("player_clone"),
            EntityType.Builder.create(CloneEntity::new, SpawnGroup.MISC)
                    .setDimensions(PlayerEntity.STANDING_DIMENSIONS.width, PlayerEntity.STANDING_DIMENSIONS.height)
                    .disableSummon()
    );

    public static final EntityType<MinionEntity> MINION = register(
            Sync.identifier("minion"),
            EntityType.Builder.create(MinionEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.5f, 0.5f)
                    .disableSummon()
    );

    public static final EntityType<CustomProjectileEntity> CUSTOM_PROJECTILE = register(
            Sync.identifier("custom_projectile"),
            EntityType.Builder.<CustomProjectileEntity>create(CustomProjectileEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.25f, 0.25f)
    );

    public static <T extends Entity> EntityType<T> register (Identifier id, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build(id.toString());
        return Registry.register(Registries.ENTITY_TYPE, id, entityType);
    }

    public static void register () {
        FabricDefaultAttributeRegistry.register(CLONE, CloneEntity.createCloneAttributes());
        FabricDefaultAttributeRegistry.register(MINION, MinionEntity.createMinionAttributes());
    }
}