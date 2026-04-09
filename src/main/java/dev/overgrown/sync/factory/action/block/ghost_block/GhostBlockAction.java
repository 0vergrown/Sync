package dev.overgrown.sync.factory.action.block.ghost_block;

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
    private static final Map<BlockPosKey, UUID> POSITION_TO_GHOST_ID = new HashMap<>();

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> triple) {
        World world = triple.getLeft();
        BlockPos originalPos = triple.getMiddle();
        Direction direction = triple.getRight();

        if (world.isClient()) {
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Identifier blockId = data.get("block");
        Block block = Registries.BLOCK.get(blockId);
        NbtCompound nbt = data.get("nbt");
        int ticks = data.getInt("tick");
        boolean addBlock = data.getBoolean("add_block");

        // Determine the target position
        BlockPos targetPos = originalPos;
        if (addBlock) {
            targetPos = originalPos.offset(direction);
        }

        // Check if there's already a ghost block at this position
        BlockPosKey posKey = new BlockPosKey(serverWorld.getRegistryKey(), targetPos);
        UUID existingGhostId = POSITION_TO_GHOST_ID.get(posKey);
        if (existingGhostId != null) {
            // Remove the existing ghost block and its timer
            GhostBlockData existingData = ACTIVE_GHOST_BLOCKS.remove(existingGhostId);
            if (existingData != null) {
                // Remove from position map
                POSITION_TO_GHOST_ID.remove(new BlockPosKey(existingData.worldKey(), existingData.pos()));

                // If we're replacing, we want to keep the ORIGINAL state from the first ghost block
                // So we'll use the existing data's original state as our base
                BlockState originalState = existingData.originalState();

                // Store the new ghost block data
                UUID newGhostId = UUID.randomUUID();
                GhostBlockData newGhostData = new GhostBlockData(
                        serverWorld.getRegistryKey(),
                        targetPos,
                        originalState,
                        newGhostId,
                        direction,
                        data.get("end_action")
                );

                // Place the ghost block
                world.setBlockState(targetPos, block.getDefaultState(), 3);

                // Set block entity data if provided
                if (nbt != null) {
                    BlockEntity blockEntity = world.getBlockEntity(targetPos);
                    if (blockEntity != null) {
                        blockEntity.readNbt(nbt);
                        blockEntity.markDirty();
                    }
                }

                // Execute block action if provided
                if (data.isPresent("block_action")) {
                    ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockAction =
                            data.get("block_action");
                    blockAction.accept(Triple.of(world, targetPos, direction));
                }

                // Store the new ghost block
                ACTIVE_GHOST_BLOCKS.put(newGhostId, newGhostData);
                POSITION_TO_GHOST_ID.put(posKey, newGhostId);

                // Schedule removal
                Apoli.SCHEDULER.queue(server -> {
                    removeGhostBlock(newGhostId, server);
                }, ticks);

                // Update clients
                world.updateListeners(targetPos, block.getDefaultState(), block.getDefaultState(), 3);
                return;
            }
        }

        // Get the original block state at the target position
        BlockState originalState = world.getBlockState(targetPos);

        // Place the ghost block
        world.setBlockState(targetPos, block.getDefaultState(), 3);

        // Set block entity data if provided
        if (nbt != null) {
            BlockEntity blockEntity = world.getBlockEntity(targetPos);
            if (blockEntity != null) {
                blockEntity.readNbt(nbt);
                blockEntity.markDirty();
            }
        }

        // Execute block action if provided
        if (data.isPresent("block_action")) {
            ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockAction =
                    data.get("block_action");
            blockAction.accept(Triple.of(world, targetPos, direction));
        }

        // Create and store ghost block data
        UUID ghostId = UUID.randomUUID();
        GhostBlockData ghostData = new GhostBlockData(
                serverWorld.getRegistryKey(),
                targetPos,
                originalState,
                ghostId,
                direction,
                data.get("end_action")
        );

        ACTIVE_GHOST_BLOCKS.put(ghostId, ghostData);
        POSITION_TO_GHOST_ID.put(posKey, ghostId);

        // Schedule removal
        Apoli.SCHEDULER.queue(server -> {
            removeGhostBlock(ghostId, server);
        }, ticks);

        // Update clients
        world.updateListeners(targetPos, block.getDefaultState(), block.getDefaultState(), 3);
    }

    private static void removeGhostBlock(UUID ghostId, net.minecraft.server.MinecraftServer server) {
        GhostBlockData data = ACTIVE_GHOST_BLOCKS.remove(ghostId);
        if (data != null) {
            // Remove from position map
            POSITION_TO_GHOST_ID.remove(new BlockPosKey(data.worldKey(), data.pos()));

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

                    // Execute end action if provided
                    if (data.endAction() != null) {
                        data.endAction().accept(Triple.of(world, pos, data.direction()));
                    }

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
                        .add("tick", SerializableDataTypes.INT, 20)
                        .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                        .add("add_block", SerializableDataTypes.BOOLEAN, false)
                        .add("end_action", ApoliDataTypes.BLOCK_ACTION, null),
                GhostBlockAction::action
        );
    }

    private record GhostBlockData(
            RegistryKey<World> worldKey,
            BlockPos pos,
            BlockState originalState,
            UUID id,
            Direction direction,
            ActionFactory<Triple<World, BlockPos, Direction>>.Instance endAction
    ) {}

    private record BlockPosKey(RegistryKey<World> worldKey, BlockPos pos) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof BlockPosKey other)) return false;
            return worldKey.equals(other.worldKey) && pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return 31 * worldKey.hashCode() + pos.hashCode();
        }
    }
}