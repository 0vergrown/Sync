package dev.overgrown.sync.factory.action.bientity.rope_leash;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.rope.common.RopeManager;
import dev.overgrown.sync.factory.data.rope.common.RopeMode;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class RopeLeashAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor = pair.getLeft();
        Entity target = pair.getRight();

        if (!(actor instanceof ServerPlayerEntity player)) return;

        RopeMode mode = data.get("mode");

        // Detach-all is target-independent — handle first so the player can
        // always release every leash without needing an aim target.
        if (mode == RopeMode.DETACH_ALL) {
            RopeManager.detachAll(player.getUuid());
            return;
        }

        if (target == null || target == actor || target.isRemoved()) return;

        int targetId = target.getId();

        switch (mode) {
            case DETACH -> RopeManager.detachByAnchorEntity(player.getUuid(), targetId);
            case ATTACH -> {
                float maxLength = data.getFloat("max_length");
                Identifier texture = data.getId("texture");
                RopeManager.attachAsLeash(player, target, maxLength, texture);
            }
            case TOGGLE -> {
                if (!RopeManager.detachByAnchorEntity(player.getUuid(), targetId)) {
                    float maxLength = data.getFloat("max_length");
                    Identifier texture = data.getId("texture");
                    RopeManager.attachAsLeash(player, target, maxLength, texture);
                }
            }
            default -> { }
        }
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("rope_leash"),
                new SerializableData()
                        .add("max_length", SerializableDataTypes.FLOAT, 10f)
                        .add("texture", SerializableDataTypes.IDENTIFIER,
                                new Identifier("sync", "textures/rope/rope.png"))
                        .add("mode", SerializableDataType.enumValue(RopeMode.class), RopeMode.TOGGLE),
                RopeLeashAction::action
        );
    }
}
