package dev.overgrown.sync.factory.action.entity.attach_rope_entity;

import dev.overgrown.sync.factory.data.rope.common.RopeManager;
import dev.overgrown.sync.factory.data.rope.common.RopeMode;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
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

public class AttachRopeEntityAction {

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                new Identifier("sync", "attach_rope_entity"),
                new SerializableData()
                        .add("max_length", SerializableDataTypes.FLOAT, 30f)
                        .add("texture", SerializableDataTypes.IDENTIFIER,
                                new Identifier("sync", "textures/rope/rope.png"))
                        .add("mode", SerializableDataType.enumValue(RopeMode.class), RopeMode.TOGGLE),
                (data, entity) -> {
                    if (!(entity instanceof ServerPlayerEntity player)) return;

                    RopeMode mode = data.get("mode");

                    if (mode == RopeMode.DETACH_ALL) {
                        RopeManager.detachAll(player.getUuid());
                        return;
                    }

                    float maxLength = data.getFloat("max_length");
                    Identifier texture = data.getId("texture");

                    // Raycast to find a target entity to anchor onto.
                    Vec3d eyePos = player.getCameraPosVec(1.0f);
                    Vec3d lookVec = player.getRotationVec(1.0f);
                    Vec3d target = eyePos.add(lookVec.multiply(maxLength));

                    Box searchBox = player.getBoundingBox()
                            .stretch(lookVec.multiply(maxLength))
                            .expand(1.0, 1.0, 1.0);

                    EntityHitResult hit = ProjectileUtil.raycast(
                            player,
                            eyePos,
                            target,
                            searchBox,
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
        );
    }
}
