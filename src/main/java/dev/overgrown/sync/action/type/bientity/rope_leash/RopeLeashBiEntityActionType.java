package dev.overgrown.sync.action.type.bientity.rope_leash;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.rope.common.RopeManager;
import dev.overgrown.sync.data.rope.common.RopeMode;
import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class RopeLeashBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<RopeLeashBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("max_length", SerializableDataTypes.FLOAT, 10f)
            .add("texture", SerializableDataTypes.IDENTIFIER, Sync.identifier("textures/rope/rope.png"))
            .add("mode", SerializableDataType.enumValue(RopeMode.class), RopeMode.TOGGLE),
        data -> new RopeLeashBiEntityActionType(
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

    public RopeLeashBiEntityActionType(float maxLength, Identifier texture, RopeMode mode) {
        this.maxLength = maxLength;
        this.texture = texture;
        this.mode = mode;
    }

    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (!(actor instanceof ServerPlayerEntity player)) return;

        if (mode == RopeMode.DETACH_ALL) {
            RopeManager.detachAll(player.getUuid());
            return;
        }

        if (target == null || target == actor || target.isRemoved()) return;

        int targetId = target.getId();

        switch (mode) {
            case DETACH -> RopeManager.detachByAnchorEntity(player.getUuid(), targetId);
            case ATTACH -> RopeManager.attachAsLeash(player, target, maxLength, texture);
            case TOGGLE -> {
                if (!RopeManager.detachByAnchorEntity(player.getUuid(), targetId)) {
                    RopeManager.attachAsLeash(player, target, maxLength, texture);
                }
            }
            default -> { }
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.ROPE_LEASH;
    }
}
