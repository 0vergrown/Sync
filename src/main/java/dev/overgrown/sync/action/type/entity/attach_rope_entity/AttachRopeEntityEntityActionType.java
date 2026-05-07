package dev.overgrown.sync.action.type.entity.attach_rope_entity;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class AttachRopeEntityEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<AttachRopeEntityEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("max_length", SerializableDataTypes.FLOAT, 30f)
            .add("texture", SerializableDataTypes.IDENTIFIER, Sync.identifier("textures/rope/rope.png"))
            .add("mode", SerializableDataType.enumValue(RopeMode.class), RopeMode.TOGGLE),
        data -> new AttachRopeEntityEntityActionType(
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

    public AttachRopeEntityEntityActionType(float maxLength, Identifier texture, RopeMode mode) {
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

        Box searchBox = player.getBoundingBox()
            .stretch(lookVec.multiply(maxLength))
            .expand(1.0, 1.0, 1.0);

        EntityHitResult hit = ProjectileUtil.raycast(
            player, eyePos, target, searchBox,
            e -> !e.isSpectator() && e.canHit() && e != player,
            maxLength * maxLength
        );

        if (hit == null) return;

        Entity hitEntity = hit.getEntity();
        int hitId = hitEntity.getId();

        switch (mode) {
            case DETACH -> RopeManager.detachByAnchorEntity(player.getUuid(), hitId);
            case ATTACH -> RopeManager.attachToEntity(player, hitEntity, maxLength, texture);
            case TOGGLE -> {
                if (!RopeManager.detachByAnchorEntity(player.getUuid(), hitId)) {
                    RopeManager.attachToEntity(player, hitEntity, maxLength, texture);
                }
            }
            default -> { }
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.ATTACH_ROPE_ENTITY;
    }
}
