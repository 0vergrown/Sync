package dev.overgrown.sync.factory.power.type.custom_projectile.entities;

import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.ActionTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionTypes;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class CustomProjectileEntity extends ThrownEntity {
    private static final TrackedData<String> TEXTURE_LOCATION = DataTracker.registerData(CustomProjectileEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> ENTITY_ID = DataTracker.registerData(CustomProjectileEntity.class, TrackedDataHandlerRegistry.STRING);

    private ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockAction;
    private ActionFactory<Pair<Entity, Entity>>.Instance missBiEntityAction;
    private ActionFactory<Pair<Entity, Entity>>.Instance impactBiEntityAction;
    private ActionFactory<Pair<Entity, Entity>>.Instance ownerImpactBiEntityAction;
    private ActionFactory<Pair<Entity, Entity>>.Instance tickBiEntityAction;
    private boolean blockActionCancelsMissAction;
    private ConditionFactory<CachedBlockPosition>.Instance blockCondition;
    private ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition;
    private ConditionFactory<Pair<Entity, Entity>>.Instance ownerBiEntityCondition;

    public CustomProjectileEntity(EntityType<? extends ThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public CustomProjectileEntity(double x, double y, double z, LivingEntity owner, World world) {
        super(SyncEntityRegistry.CUSTOM_PROJECTILE, x, y, z, world);
        setOwner(owner);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TEXTURE_LOCATION, "");
        this.dataTracker.startTracking(ENTITY_ID, "");
    }

    @Override
    protected boolean canHit(Entity target) {
        if (!super.canHit(target)) return false;
        Entity owner = getOwner();
        if (biEntityCondition != null && !biEntityCondition.test(new Pair<>(this, target))) return false;
        if (owner != null && ownerBiEntityCondition != null && !ownerBiEntityCondition.test(new Pair<>(owner, target))) return false;
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();
        if (impactBiEntityAction != null) {
            impactBiEntityAction.accept(new Pair<>(this, target));
        }
        Entity owner = getOwner();
        if (ownerImpactBiEntityAction != null && owner != null) {
            ownerImpactBiEntityAction.accept(new Pair<>(owner, target));
        }
        discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        boolean executedBlockAction = false;
        BlockPos pos = blockHitResult.getBlockPos();
        Direction dir = blockHitResult.getSide();

        if (blockAction != null) {
            boolean conditionPasses = blockCondition == null || blockCondition.test(new CachedBlockPosition(getWorld(), pos, false));
            if (conditionPasses) {
                blockAction.accept(Triple.of(getWorld(), pos, dir));
                executedBlockAction = true;
            }
        }

        if (!executedBlockAction || !blockActionCancelsMissAction) {
            Entity owner = getOwner();
            if (missBiEntityAction != null && owner != null) {
                missBiEntityAction.accept(new Pair<>(owner, this));
            }
        }

        BlockState blockState = getWorld().getBlockState(pos);
        blockState.onProjectileHit(getWorld(), blockState, blockHitResult, this);
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null && owner.isRemoved()) {
            discard();
            return;
        }
        if (tickBiEntityAction != null && owner != null) {
            tickBiEntityAction.accept(new Pair<>(owner, this));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        String tex = this.dataTracker.get(TEXTURE_LOCATION);
        if (!tex.isEmpty()) nbt.putString("TextureLocation", tex);
        String eid = this.dataTracker.get(ENTITY_ID);
        if (!eid.isEmpty()) nbt.putString("EntityId", eid);
        nbt.putBoolean("BlockActionCancelsMissAction", blockActionCancelsMissAction);

        writeBiEntityAction(nbt, "ImpactBiEntityAction", impactBiEntityAction);
        writeBiEntityAction(nbt, "MissBiEntityAction", missBiEntityAction);
        writeBiEntityAction(nbt, "OwnerImpactBiEntityAction", ownerImpactBiEntityAction);
        writeBiEntityAction(nbt, "TickBiEntityAction", tickBiEntityAction);
        writeBlockAction(nbt, "ImpactBlockAction", blockAction);
        writeBiEntityCondition(nbt, "BiEntityCondition", biEntityCondition);
        writeBiEntityCondition(nbt, "OwnerBiEntityCondition", ownerBiEntityCondition);
        writeBlockCondition(nbt, "BlockCondition", blockCondition);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TextureLocation")) this.dataTracker.set(TEXTURE_LOCATION, nbt.getString("TextureLocation"));
        if (nbt.contains("EntityId")) this.dataTracker.set(ENTITY_ID, nbt.getString("EntityId"));
        blockActionCancelsMissAction = nbt.getBoolean("BlockActionCancelsMissAction");

        impactBiEntityAction = readBiEntityAction(nbt, "ImpactBiEntityAction");
        missBiEntityAction = readBiEntityAction(nbt, "MissBiEntityAction");
        ownerImpactBiEntityAction = readBiEntityAction(nbt, "OwnerImpactBiEntityAction");
        tickBiEntityAction = readBiEntityAction(nbt, "TickBiEntityAction");
        blockAction = readBlockAction(nbt, "ImpactBlockAction");
        biEntityCondition = readBiEntityCondition(nbt, "BiEntityCondition");
        ownerBiEntityCondition = readBiEntityCondition(nbt, "OwnerBiEntityCondition");
        blockCondition = readBlockCondition(nbt, "BlockCondition");
    }

    // --- NBT serialization helpers using PacketByteBuf ---

    private void writeBiEntityAction(NbtCompound nbt, String key, ActionFactory<Pair<Entity, Entity>>.Instance action) {
        if (action == null) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ActionTypes.BIENTITY.write(buf, action);
        nbt.putByteArray(key, extractBytes(buf));
    }

    private ActionFactory<Pair<Entity, Entity>>.Instance readBiEntityAction(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return null;
        try {
            return ActionTypes.BIENTITY.read(new PacketByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key))));
        } catch (Exception e) {
            return null;
        }
    }

    private void writeBlockAction(NbtCompound nbt, String key, ActionFactory<Triple<World, BlockPos, Direction>>.Instance action) {
        if (action == null) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ActionTypes.BLOCK.write(buf, action);
        nbt.putByteArray(key, extractBytes(buf));
    }

    private ActionFactory<Triple<World, BlockPos, Direction>>.Instance readBlockAction(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return null;
        try {
            return ActionTypes.BLOCK.read(new PacketByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key))));
        } catch (Exception e) {
            return null;
        }
    }

    private void writeBiEntityCondition(NbtCompound nbt, String key, ConditionFactory<Pair<Entity, Entity>>.Instance condition) {
        if (condition == null) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ConditionTypes.BIENTITY.write(buf, condition);
        nbt.putByteArray(key, extractBytes(buf));
    }

    private ConditionFactory<Pair<Entity, Entity>>.Instance readBiEntityCondition(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return null;
        try {
            return ConditionTypes.BIENTITY.read(new PacketByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key))));
        } catch (Exception e) {
            return null;
        }
    }

    private void writeBlockCondition(NbtCompound nbt, String key, ConditionFactory<CachedBlockPosition>.Instance condition) {
        if (condition == null) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ConditionTypes.BLOCK.write(buf, condition);
        nbt.putByteArray(key, extractBytes(buf));
    }

    private ConditionFactory<CachedBlockPosition>.Instance readBlockCondition(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return null;
        try {
            return ConditionTypes.BLOCK.read(new PacketByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key))));
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] extractBytes(PacketByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return bytes;
    }

    // --- Getters / Setters ---

    public Identifier getTextureId() {
        String tex = this.dataTracker.get(TEXTURE_LOCATION);
        if (tex.isEmpty()) return null;
        return new Identifier(tex);
    }

    public void setTextureLocation(Identifier location) {
        if (location != null) {
            this.dataTracker.set(TEXTURE_LOCATION, location.toString());
        }
    }

    public Identifier getEntityId() {
        String id = this.dataTracker.get(ENTITY_ID);
        if (id.isEmpty()) return null;
        return new Identifier(id);
    }

    public void setEntityId(Identifier id) {
        if (id != null) {
            this.dataTracker.set(ENTITY_ID, id.toString());
        }
    }

    public void setBlockAction(ActionFactory<Triple<World, BlockPos, Direction>>.Instance action) {
        this.blockAction = action;
    }

    public void setMissBiEntityAction(ActionFactory<Pair<Entity, Entity>>.Instance action) {
        this.missBiEntityAction = action;
    }

    public void setImpactBiEntityAction(ActionFactory<Pair<Entity, Entity>>.Instance action) {
        this.impactBiEntityAction = action;
    }

    public void setOwnerImpactBiEntityAction(ActionFactory<Pair<Entity, Entity>>.Instance action) {
        this.ownerImpactBiEntityAction = action;
    }

    public void setTickBiEntityAction(ActionFactory<Pair<Entity, Entity>>.Instance action) {
        this.tickBiEntityAction = action;
    }

    public void setBlockActionCancelsMissAction(boolean value) {
        this.blockActionCancelsMissAction = value;
    }

    public void setBlockCondition(ConditionFactory<CachedBlockPosition>.Instance condition) {
        this.blockCondition = condition;
    }

    public void setBiEntityCondition(ConditionFactory<Pair<Entity, Entity>>.Instance condition) {
        this.biEntityCondition = condition;
    }

    public void setOwnerBiEntityCondition(ConditionFactory<Pair<Entity, Entity>>.Instance condition) {
        this.ownerBiEntityCondition = condition;
    }
}
