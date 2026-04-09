package dev.overgrown.sync.factory.action.entity.change_selected_slot;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Changes the player's currently selected hotbar slot.
 *
 * <p>The hotbar has exactly 9 slots, indexed 0 (far left) through 8 (far right).
 * Values outside this range are clamped automatically.</p>
 *
 * <p>JSON example — select the middle hotbar slot:</p>
 * <pre>{@code
 * {
 *   "type": "sync:change_selected_slot",
 *   "slot": 4
 * }
 * }</pre>
 */
public class ChangeSelectedSlotAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return;

        // Hotbar: 9 slots, indices 0–8.
        // PlayerInventory.isValidHotbarIndex(slot) checks: slot >= 0 && slot < 9
        int slot = MathHelper.clamp(data.getInt("slot"), 0, PlayerInventory.getHotbarSize() - 1);

        // Set on the server's copy of the inventory.
        player.getInventory().selectedSlot = slot;

        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        // Push the new selected slot to the client so the hotbar highlight
        // and held-item visual update immediately.
        serverPlayer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(slot));

        // Force the screen handler to resync its tracked stacks so items
        // held by the cursor or active item slots reflect the new selection.
        serverPlayer.playerScreenHandler.sendContentUpdates();
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("change_selected_slot"),
                new SerializableData()
                        // 0 = far-left hotbar slot, 8 = far-right hotbar slot.
                        .add("slot", SerializableDataTypes.INT),
                ChangeSelectedSlotAction::action
        );
    }
}