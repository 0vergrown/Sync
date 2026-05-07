package dev.overgrown.sync.action.type.entity.change_selected_slot;

import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ChangeSelectedSlotEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ChangeSelectedSlotEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("slot", SerializableDataTypes.INT),
        data -> new ChangeSelectedSlotEntityActionType(data.get("slot")),
        (actionType, serializableData) -> serializableData.instance()
            .set("slot", actionType.slot)
    );

    private final int slot;

    public ChangeSelectedSlotEntityActionType(int slot) {
        this.slot = slot;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (!(context.entity() instanceof PlayerEntity player)) return;

        int target = MathHelper.clamp(slot, 0, PlayerInventory.getHotbarSize() - 1);
        player.getInventory().selectedSlot = target;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(target));
            serverPlayer.playerScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.CHANGE_SELECTED_SLOT;
    }
}
