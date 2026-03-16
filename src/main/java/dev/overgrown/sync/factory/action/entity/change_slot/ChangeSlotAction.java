package dev.overgrown.sync.factory.action.entity.change_slot;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

/**
 * Swaps or moves items between two inventory slots.
 *
 * <p>Compatible with Apoli's {@code inventory_type} / {@code power} system so it
 * works with both the player inventory and {@code apoli:inventory} power inventories.</p>
 *
 * <p>Use Apoli's standard slot names for {@code slot_a} / {@code slot_b}, e.g.
 * {@code "hotbar.0"}, {@code "weapon.mainhand"}, {@code "armor.head"}, {@code "container.0"}, etc.</p>
 *
 * <table>
 *   <tr><th>operation</th><th>effect</th></tr>
 *   <tr><td>{@code swap}</td><td>exchanges the two stacks</td></tr>
 *   <tr><td>{@code move}</td><td>places slot_a's stack into slot_b, clears slot_a (slot_b's previous contents are discarded)</td></tr>
 * </table>
 *
 * <p>JSON examples:</p>
 * <pre>{@code
 * // Swap main-hand and off-hand:
 * {
 *   "type": "sync:change_slot",
 *   "slot_a": "weapon.mainhand",
 *   "slot_b": "weapon.offhand",
 *   "operation": "swap"
 * }
 *
 * // Move slot 0 of a power inventory to slot 1:
 * {
 *   "type": "sync:change_slot",
 *   "slot_a": "container.0",
 *   "slot_b": "container.1",
 *   "operation": "move",
 *   "inventory_type": "power",
 *   "power": "example:my_inventory_power"
 * }
 * }</pre>
 */
public class ChangeSlotAction {

    public enum SlotOperation {
        SWAP,
        MOVE
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        InventoryType inventoryType = data.get("inventory_type");
        SlotOperation operation = data.get("operation");

        int slotA = data.<ArgumentWrapper<Integer>>get("slot_a").get();
        int slotB = data.<ArgumentWrapper<Integer>>get("slot_b").get();

        switch (inventoryType) {
            case INVENTORY -> handleInventory(entity, slotA, slotB, operation);
            case POWER     -> handlePower(data, entity, slotA, slotB, operation);
        }
    }

    // -----------------------------------------------------------------------
    // Player / entity inventory (uses Apoli's StackReference system)
    // -----------------------------------------------------------------------
    private static void handleInventory(Entity entity, int slotA, int slotB, SlotOperation operation) {
        StackReference refA = entity.getStackReference(slotA);
        StackReference refB = entity.getStackReference(slotB);

        if (refA == StackReference.EMPTY || refB == StackReference.EMPTY) return;

        ItemStack stackA = refA.get().copy();
        ItemStack stackB = refB.get().copy();

        switch (operation) {
            case SWAP -> {
                refA.set(stackB);
                refB.set(stackA);
            }
            case MOVE -> {
                refA.set(ItemStack.EMPTY);
                refB.set(stackA);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Apoli InventoryPower inventory
    // -----------------------------------------------------------------------
    private static void handlePower(SerializableData.Instance data, Entity entity,
                                    int slotA, int slotB, SlotOperation operation) {
        if (!data.isPresent("power")) return;

        PowerHolderComponent.KEY.maybeGet(entity).ifPresent(phc -> {
            PowerType<?> powerType = data.get("power");
            Power power = phc.getPower(powerType);
            if (!(power instanceof InventoryPower inv)) return;

            // Validate slot indices against the power's container size.
            if (slotA < 0 || slotA >= inv.size() || slotB < 0 || slotB >= inv.size()) return;

            ItemStack stackA = inv.getStack(slotA).copy();
            ItemStack stackB = inv.getStack(slotB).copy();

            switch (operation) {
                case SWAP -> {
                    inv.setStack(slotA, stackB);
                    inv.setStack(slotB, stackA);
                }
                case MOVE -> {
                    inv.setStack(slotA, ItemStack.EMPTY);
                    inv.setStack(slotB, stackA);
                }
            }
        });
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("change_slot"),
                new SerializableData()
                        .add("slot_a",        ApoliDataTypes.ITEM_SLOT)
                        .add("slot_b",        ApoliDataTypes.ITEM_SLOT)
                        .add("operation",     SerializableDataType.enumValue(SlotOperation.class), SlotOperation.SWAP)
                        .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
                        .add("power",         ApoliDataTypes.POWER_TYPE, null),
                ChangeSlotAction::action
        );
    }
}