package dev.overgrown.sync.factory.action.block;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostBlockAction {

    private static final Map<UUID, GhostBlockData> ACTIVE_GHOST_BLOCKS = new HashMap<>();

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> triple) {
        World world = triple.getLeft();
        BlockPos pos = triple.getMiddle();
        Direction direction = triple.getRight();

        if (world.isClient()) {
            return;
        }

        // Ensure we're on a ServerWorld
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Identifier blockId = data.get("block");
        Block block = Registries.BLOCK.get(blockId);
        NbtCompound nbt = data.get("nbt");
        int ticks = data.getInt("tick");

        BlockState originalState = world.getBlockState(pos);

        // Place the ghost block
        world.setBlockState(pos, block.getDefaultState(), 3);

        // Set block entity data if provided
        if (nbt != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntity.readNbt(nbt);
                blockEntity.markDirty();
            }
        }

        // Execute block action if provided
        if (data.isPresent("block_action")) {
            ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockAction =
                    data.get("block_action");
            blockAction.accept(Triple.of(world, pos, direction));
        }

        // Schedule removal
        UUID ghostId = UUID.randomUUID();
        GhostBlockData ghostData = new GhostBlockData(
                serverWorld.getRegistryKey(),  // Store RegistryKey instead of Identifier
                pos,
                originalState,
                ghostId
        );
        ACTIVE_GHOST_BLOCKS.put(ghostId, ghostData);

        // Schedule removal using Apoli's scheduler
        Apoli.SCHEDULER.queue(server -> {
            removeGhostBlock(ghostId, server);
        }, ticks);

        // Also schedule client-side update
        world.updateListeners(pos, block.getDefaultState(), block.getDefaultState(), 3);
    }

    private static void removeGhostBlock(UUID ghostId, net.minecraft.server.MinecraftServer server) {
        GhostBlockData data = ACTIVE_GHOST_BLOCKS.remove(ghostId);
        if (data != null) {
            // Get the world from the server using the stored RegistryKey
            ServerWorld world = server.getWorld(data.worldKey());
            if (world != null) {
                BlockPos pos = data.pos();

                // Check if the chunk is loaded
                boolean chunkLoaded = world.getChunkManager().isChunkLoaded(
                        pos.getX() >> 4,
                        pos.getZ() >> 4
                );

                if (chunkLoaded) {
                    // Restore original block
                    world.setBlockState(pos, data.originalState(), 3);

                    // Play break sound and particles
                    world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(data.originalState()));

                    // Update clients
                    world.updateListeners(pos, data.originalState(), data.originalState(), 3);
                }
            }
        }
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("ghost_block"),
                new SerializableData()
                        .add("block", SerializableDataTypes.IDENTIFIER)
                        .add("nbt", SerializableDataTypes.NBT, null)
                        .add("tick", SerializableDataTypes.INT, 20) // Default 1 second
                        .add("block_action", ApoliDataTypes.BLOCK_ACTION, null),
                GhostBlockAction::action
        );
    }

    private record GhostBlockData(
            RegistryKey<World> worldKey,
            BlockPos pos,
            BlockState originalState,
            UUID id
    ) {}
}