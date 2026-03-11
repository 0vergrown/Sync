package dev.overgrown.sync.factory.action.entity.attach_rope;

import dev.overgrown.sync.rope.common.RopeManager;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
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
                                new Identifier("sync", "textures/entity/rope.png")),
                (data, entity) -> {
                    if (!(entity instanceof ServerPlayerEntity player)) return;

                    // Toggle: if already attached, detach
                    if (RopeManager.has(player.getUuid())) {
                        RopeManager.detach(player);
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
                    RopeManager.attach(player, anchor, maxLength, texture);
                }
        );
    }
}