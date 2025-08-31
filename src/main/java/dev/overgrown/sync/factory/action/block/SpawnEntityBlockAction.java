package dev.overgrown.sync.factory.action.block;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Consumer;

public class SpawnEntityBlockAction {

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> triple) {
        World world = triple.getLeft();
        BlockPos pos = triple.getMiddle();

        if (!world.isClient()) {
            EntityType<?> entityType = data.get("entity_type");
            NbtCompound nbt = data.get("tag");

            Optional<Entity> optionalEntity = MiscUtil.getEntityWithPassengers(
                    world, entityType, nbt, Vec3d.ofCenter(pos), 0.0f, 0.0f
            );

            if (optionalEntity.isPresent()) {
                Entity entity = optionalEntity.get();
                world.spawnEntity(entity);

                if (data.isPresent("entity_action")) {
                    Consumer<Entity> entityAction = data.get("entity_action");
                    entityAction.accept(entity);
                }
            }
        }
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("spawn_entity"),
                new SerializableData()
                        .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                        .add("tag", SerializableDataTypes.NBT, null)
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
                SpawnEntityBlockAction::action
        );
    }
}