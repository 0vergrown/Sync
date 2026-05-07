package dev.overgrown.sync.power.type.custom_projectile.entities;

import dev.overgrown.sync.registry.SyncEntityRegistry;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class CustomProjectileEntity extends ThrownEntity {

    private static final TrackedData<String> TEXTURE_LOCATION =
        DataTracker.registerData(CustomProjectileEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> ENTITY_ID =
        DataTracker.registerData(CustomProjectileEntity.class, TrackedDataHandlerRegistry.STRING);

    private BlockAction blockAction;
    private BiEntityAction missBiEntityAction;
    private BiEntityAction impactBiEntityAction;
    private BiEntityAction ownerImpactBiEntityAction;
    private BiEntityAction tickBiEntityAction;
    private boolean blockActionCancelsMissAction;
    private BlockCondition blockCondition;
    private BiEntityCondition biEntityCondition;
    private BiEntityCondition ownerBiEntityCondition;

    public CustomProjectileEntity(EntityType<? extends ThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public CustomProjectileEntity(double x, double y, double z, LivingEntity owner, World world) {
        super(SyncEntityRegistry.CUSTOM_PROJECTILE, x, y, z, world);
        setOwner(owner);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TEXTURE_LOCATION, "");
        builder.add(ENTITY_ID, "");
    }

    @Override
    protected boolean canHit(Entity target) {
        if (!super.canHit(target)) return false;
        Entity owner = getOwner();
        if (biEntityCondition != null && !biEntityCondition.test(this, target)) return false;
        if (owner != null && ownerBiEntityCondition != null && !ownerBiEntityCondition.test(owner, target)) return false;
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();
        if (impactBiEntityAction != null) {
            impactBiEntityAction.accept(new BiEntityActionContext(this, target));
        }
        Entity owner = getOwner();
        if (ownerImpactBiEntityAction != null && owner != null) {
            ownerImpactBiEntityAction.accept(new BiEntityActionContext(owner, target));
        }
        discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        boolean executedBlockAction = false;
        BlockPos pos = blockHitResult.getBlockPos();
        Direction dir = blockHitResult.getSide();

        if (blockAction != null && getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            boolean conditionPasses = blockCondition == null
                || blockCondition.test(new BlockConditionContext(serverWorld, pos));
            if (conditionPasses) {
                blockAction.accept(new BlockActionContext(serverWorld, pos, Optional.of(dir)));
                executedBlockAction = true;
            }
        }

        if (!executedBlockAction || !blockActionCancelsMissAction) {
            Entity owner = getOwner();
            if (missBiEntityAction != null && owner != null) {
                missBiEntityAction.accept(new BiEntityActionContext(owner, this));
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
            tickBiEntityAction.accept(new BiEntityActionContext(owner, this));
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
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TextureLocation")) this.dataTracker.set(TEXTURE_LOCATION, nbt.getString("TextureLocation"));
        if (nbt.contains("EntityId")) this.dataTracker.set(ENTITY_ID, nbt.getString("EntityId"));
        blockActionCancelsMissAction = nbt.getBoolean("BlockActionCancelsMissAction");
    }

    public Identifier getTextureId() {
        String tex = this.dataTracker.get(TEXTURE_LOCATION);
        if (tex.isEmpty()) return null;
        return Identifier.tryParse(tex);
    }

    public void setTextureLocation(Identifier location) {
        if (location != null) {
            this.dataTracker.set(TEXTURE_LOCATION, location.toString());
        }
    }

    public Identifier getEntityId() {
        String id = this.dataTracker.get(ENTITY_ID);
        if (id.isEmpty()) return null;
        return Identifier.tryParse(id);
    }

    public void setEntityId(Identifier id) {
        if (id != null) {
            this.dataTracker.set(ENTITY_ID, id.toString());
        }
    }

    public void setBlockAction(BlockAction action) { this.blockAction = action; }
    public void setMissBiEntityAction(BiEntityAction action) { this.missBiEntityAction = action; }
    public void setImpactBiEntityAction(BiEntityAction action) { this.impactBiEntityAction = action; }
    public void setOwnerImpactBiEntityAction(BiEntityAction action) { this.ownerImpactBiEntityAction = action; }
    public void setTickBiEntityAction(BiEntityAction action) { this.tickBiEntityAction = action; }
    public void setBlockActionCancelsMissAction(boolean value) { this.blockActionCancelsMissAction = value; }
    public void setBlockCondition(BlockCondition condition) { this.blockCondition = condition; }
    public void setBiEntityCondition(BiEntityCondition condition) { this.biEntityCondition = condition; }
    public void setOwnerBiEntityCondition(BiEntityCondition condition) { this.ownerBiEntityCondition = condition; }
}
