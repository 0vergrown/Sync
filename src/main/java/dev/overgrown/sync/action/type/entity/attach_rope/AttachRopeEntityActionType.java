package dev.overgrown.sync.action.type.entity.attach_rope;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.rope.common.RopeManager;
import dev.overgrown.sync.data.rope.common.RopeMode;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;

public class AttachRopeEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<AttachRopeEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("max_length", SerializableDataTypes.FLOAT, 30f)
            .add("texture", SerializableDataTypes.IDENTIFIER, Sync.identifier("textures/rope/rope.png"))
            .add("mode", SerializableDataType.enumValue(RopeMode.class), RopeMode.TOGGLE),
        data -> new AttachRopeEntityActionType(
            data.get("max_length"),
            data.get("texture"),
            data.get("mode")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("max_length", actionType.maxLength)
            .set("texture", actionType.texture)
            .set("mode", actionType.mode)
    );

    private final float maxLength;
    private final Identifier texture;
    private final RopeMode mode;

    public AttachRopeEntityActionType(float maxLength, Identifier texture, RopeMode mode) {
        this.maxLength = maxLength;
        this.texture = texture;
        this.mode = mode;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (!(context.entity() instanceof ServerPlayerEntity player)) return;

        if (mode == RopeMode.DETACH_ALL) {
            RopeManager.detachAll(player.getUuid());
            return;
        }

        Vec3d eyePos = player.getCameraPosVec(1.0f);
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d target = eyePos.add(lookVec.multiply(maxLength));

        BlockHitResult hit = player.getWorld().raycast(new RaycastContext(
            eyePos, target,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));

        if (hit.getType() == HitResult.Type.MISS) return;

        Vec3d anchor = hit.getPos();

        switch (mode) {
            case DETACH -> RopeManager.detachByAnchorPos(player.getUuid(), anchor);
            case ATTACH -> RopeManager.attach(player, anchor, maxLength, texture);
            case TOGGLE -> {
                if (!RopeManager.detachByAnchorPos(player.getUuid(), anchor)) {
                    RopeManager.attach(player, anchor, maxLength, texture);
                }
            }
            default -> { }
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.ATTACH_ROPE;
    }
}
