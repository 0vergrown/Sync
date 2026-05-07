package dev.overgrown.sync.action.type.block.ghost_block;

import dev.overgrown.sync.registry.SyncBlockActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GhostBlockActionType extends BlockActionType {

    private static final Scheduler SCHEDULER = new Scheduler();
    private static final Map<UUID, GhostBlockData> ACTIVE_GHOST_BLOCKS = new HashMap<>();
    private static final Map<BlockPosKey, UUID> POSITION_TO_GHOST_ID = new HashMap<>();

    public static final TypedDataObjectFactory<GhostBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block", SerializableDataTypes.IDENTIFIER)
            .add("nbt", SerializableDataTypes.NBT_COMPOUND.optional(), Optional.empty())
            .add("tick", SerializableDataTypes.INT, 20)
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("add_block", SerializableDataTypes.BOOLEAN, false)
            .add("end_action", BlockAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new GhostBlockActionType(
            data.get("block"),
            data.get("nbt"),
            data.get("tick"),
            data.get("block_action"),
            data.get("add_block"),
            data.get("end_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block", actionType.blockId)
            .set("nbt", actionType.nbt)
            .set("tick", actionType.ticks)
            .set("block_action", actionType.blockAction)
            .set("add_block", actionType.addBlock)
            .set("end_action", actionType.endAction)
    );

    private final Identifier blockId;
    private final Optional<NbtCompound> nbt;
    private final int ticks;
    private final Optional<BlockAction> blockAction;
    private final boolean addBlock;
    private final Optional<BlockAction> endAction;

    public GhostBlockActionType(Identifier blockId, Optional<NbtCompound> nbt, int ticks,
                                Optional<BlockAction> blockAction, boolean addBlock,
                                Optional<BlockAction> endAction) {
        this.blockId = blockId;
        this.nbt = nbt;
        this.ticks = ticks;
        this.blockAction = blockAction;
        this.addBlock = addBlock;
        this.endAction = endAction;
    }

    @Override
    public void accept(BlockActionContext context) {
        ServerWorld world = context.world();
        if (world == null) return;

        BlockPos originalPos = context.pos();
        Direction direction = context.direction().orElse(Direction.UP);
        Block block = Registries.BLOCK.get(blockId);

        BlockPos targetPos = addBlock ? originalPos.offset(direction) : originalPos;
        BlockPosKey posKey = new BlockPosKey(world.getRegistryKey(), targetPos);

        BlockState baseOriginalState;
        UUID existingGhostId = POSITION_TO_GHOST_ID.get(posKey);
        if (existingGhostId != null) {
            GhostBlockData existingData = ACTIVE_GHOST_BLOCKS.remove(existingGhostId);
            if (existingData != null) {
                POSITION_TO_GHOST_ID.remove(new BlockPosKey(existingData.worldKey(), existingData.pos()));
                baseOriginalState = existingData.originalState();
            } else {
                baseOriginalState = world.getBlockState(targetPos);
            }
        } else {
            baseOriginalState = world.getBlockState(targetPos);
        }

        world.setBlockState(targetPos, block.getDefaultState(), 3);

        if (nbt.isPresent()) {
            BlockEntity blockEntity = world.getBlockEntity(targetPos);
            if (blockEntity != null) {
                blockEntity.read(nbt.get(), world.getRegistryManager());
                blockEntity.markDirty();
            }
        }

        blockAction.ifPresent(act -> act.accept(new BlockActionContext(world, targetPos, Optional.of(direction))));

        UUID ghostId = UUID.randomUUID();
        GhostBlockData ghostData = new GhostBlockData(
            world.getRegistryKey(), targetPos, baseOriginalState, ghostId, direction, endAction
        );

        ACTIVE_GHOST_BLOCKS.put(ghostId, ghostData);
        POSITION_TO_GHOST_ID.put(posKey, ghostId);

        SCHEDULER.queue(server -> removeGhostBlock(ghostId, server), ticks);

        world.updateListeners(targetPos, block.getDefaultState(), block.getDefaultState(), 3);
    }

    private static void removeGhostBlock(UUID ghostId, MinecraftServer server) {
        GhostBlockData data = ACTIVE_GHOST_BLOCKS.remove(ghostId);
        if (data == null) return;

        POSITION_TO_GHOST_ID.remove(new BlockPosKey(data.worldKey(), data.pos()));

        ServerWorld world = server.getWorld(data.worldKey());
        if (world == null) return;

        BlockPos pos = data.pos();
        boolean chunkLoaded = world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);

        if (chunkLoaded) {
            world.setBlockState(pos, data.originalState(), 3);
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(data.originalState()));
            data.endAction().ifPresent(end ->
                end.accept(new BlockActionContext(world, pos, Optional.of(data.direction()))));
            world.updateListeners(pos, data.originalState(), data.originalState(), 3);
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBlockActionTypes.GHOST_BLOCK;
    }

    private record GhostBlockData(
        RegistryKey<World> worldKey,
        BlockPos pos,
        BlockState originalState,
        UUID id,
        Direction direction,
        Optional<BlockAction> endAction
    ) {}

    private record BlockPosKey(RegistryKey<World> worldKey, BlockPos pos) {}
}
