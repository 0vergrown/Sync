package dev.overgrown.sync.factory.action.entity.attach_rope;

import dev.overgrown.sync.factory.data.rope.common.RopeManager;
import dev.overgrown.sync.factory.data.rope.common.RopeMode;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AttachRopeAction {

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                new Identifier("sync", "attach_rope"),
                new SerializableData()
                        .add("max_length", SerializableDataTypes.FLOAT, 30f)
                        .add("texture", SerializableDataTypes.IDENTIFIER,
                                new Identifier("sync", "textures/rope/rope.png"))
                        .add("mode", SerializableDataType.enumValue(RopeMode.class), RopeMode.TOGGLE),
                (data, entity) -> {
                    if (!(entity instanceof ServerPlayerEntity player)) return;

                    RopeMode mode = data.get("mode");

                    // Detach-all is target-agnostic; handle it before any
                    // raycasting so players can clear ropes without aiming.
                    if (mode == RopeMode.DETACH_ALL) {
                        RopeManager.detachAll(player.getUuid());
                        return;
                    }

                    float maxLength = data.getFloat("max_length");
                    Identifier texture = data.getId("texture");

                    // Raycast to find anchor block
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
        );
    }
}
