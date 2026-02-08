package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.entities.custom_projectile.CustomProjectileEntity;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

public class CustomProjectileAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.getWorld().isClient() || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Execute entity action before firing if specified
        if (data.isPresent("entity_action_before_firing")) {
            data.<io.github.apace100.apoli.power.factory.action.ActionFactory<Entity>.Instance>get("entity_action_before_firing")
                    .accept(livingEntity);
        }

        int count = data.getInt("count");
        float speed = data.getFloat("speed");
        float divergence = data.getFloat("divergence");
        Identifier textureLocation = data.getId("texture_location");
        Identifier entityId = data.getId("entity_id");
        NbtCompound tag = data.get("tag");

        // Play sound if specified
        if (data.isPresent("sound")) {
            SoundEvent sound = data.get("sound");
            livingEntity.getWorld().playSound(
                    null,
                    livingEntity.getX(),
                    livingEntity.getY(),
                    livingEntity.getZ(),
                    sound,
                    SoundCategory.NEUTRAL,
                    0.5F,
                    0.4F / (livingEntity.getRandom().nextFloat() * 0.4F + 0.8F)
            );
        }

        for (int i = 0; i < count; i++) {
            Vec3d rotationVec = livingEntity.getRotationVector();
            Vec3d spawnPos = new Vec3d(
                    livingEntity.getX(),
                    livingEntity.getEyeY(),
                    livingEntity.getZ()
            ).add(rotationVec);

            CustomProjectileEntity projectile = new CustomProjectileEntity(
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    livingEntity,
                    livingEntity.getWorld()
            );

            // Set velocity with divergence
            float adjustedDivergence = (i == 0) ? 0.0F : divergence * 0.075F;
            projectile.setVelocity(
                    livingEntity,
                    livingEntity.getPitch(),
                    livingEntity.getYaw(),
                    0.0F,
                    speed,
                    adjustedDivergence
            );

            // Set identification and texture
            if (entityId != null) {
                projectile.setEntityId(entityId);
            }
            if (textureLocation != null) {
                projectile.setTextureLocation(textureLocation);
            }

            // Set NBT data if specified
            if (tag != null) {
                NbtCompound merged = projectile.writeNbt(new NbtCompound());
                merged.copyFrom(tag);
                projectile.readNbt(merged);
            }

            // Set actions and conditions
            if (data.isPresent("block_action_on_hit")) {
                projectile.setBlockAction(data.get("block_action_on_hit"));
            }
            if (data.isPresent("bientity_action_on_miss")) {
                projectile.setMissBiEntityAction(data.get("bientity_action_on_miss"));
            }
            if (data.isPresent("bientity_action_on_hit")) {
                projectile.setImpactBiEntityAction(data.get("bientity_action_on_hit"));
            }
            if (data.isPresent("owner_target_bientity_action_on_hit")) {
                projectile.setOwnerImpactBiEntityAction(data.get("owner_target_bientity_action_on_hit"));
            }
            if (data.isPresent("tick_bientity_action")) {
                projectile.setTickBiEntityAction(data.get("tick_bientity_action"));
            }

            // Set block action cancellation
            if (data.isPresent("block_action_cancels_miss_action")) {
                projectile.setBlockActionCancelsMissAction(data.getBoolean("block_action_cancels_miss_action"));
            }

            // Set conditions
            if (data.isPresent("block_condition")) {
                projectile.setBlockCondition(data.get("block_condition"));
            }
            if (data.isPresent("bientity_condition")) {
                projectile.setBiEntityCondition(data.get("bientity_condition"));
            }
            if (data.isPresent("owner_bientity_condition")) {
                projectile.setOwnerBiEntityCondition(data.get("owner_bientity_condition"));
            }

            // Spawn the projectile
            livingEntity.getWorld().spawnEntity(projectile);

            // Execute bi-entity action after firing if specified
            if (data.isPresent("bientity_action_after_firing")) {
                data.<io.github.apace100.apoli.power.factory.action.ActionFactory<Pair<Entity, Entity>>.Instance>get("bientity_action_after_firing")
                        .accept(new Pair<>(livingEntity, projectile));
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("custom_projectile"),
                new SerializableData()
                        .add("entity_id", SerializableDataTypes.IDENTIFIER, null)
                        .add("texture_location", SerializableDataTypes.IDENTIFIER, null)
                        .add("count", SerializableDataTypes.INT, 1)
                        .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                        .add("divergence", SerializableDataTypes.FLOAT, 1.0F)
                        .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                        .add("tag", SerializableDataTypes.NBT, null)
                        .add("entity_action_before_firing", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("bientity_action_after_firing", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("block_action_on_hit", ApoliDataTypes.BLOCK_ACTION, null)
                        .add("bientity_action_on_miss", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("bientity_action_on_hit", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("owner_target_bientity_action_on_hit", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("block_action_cancels_miss_action", SerializableDataTypes.BOOLEAN, false)
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("owner_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("tick_bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
                CustomProjectileAction::action
        );
    }
}