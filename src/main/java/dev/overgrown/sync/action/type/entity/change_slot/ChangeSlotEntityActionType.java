package dev.overgrown.sync.action.type.entity.change_slot;

import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChangeSlotEntityActionType extends EntityActionType {

    public enum SlotOperation {
        SWAP,
        MOVE
    }

    public static final TypedDataObjectFactory<ChangeSlotEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("slot_a", ApoliDataTypes.ITEM_SLOT)
            .add("slot_b", ApoliDataTypes.ITEM_SLOT)
            .add("operation", SerializableDataType.enumValue(SlotOperation.class), SlotOperation.SWAP)
            .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty()),
        data -> new ChangeSlotEntityActionType(
            data.get("slot_a"),
            data.get("slot_b"),
            data.get("operation"),
            data.get("inventory_type"),
            data.get("power")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("slot_a", actionType.slotA)
            .set("slot_b", actionType.slotB)
            .set("operation", actionType.operation)
            .set("inventory_type", actionType.inventoryType)
            .set("power", actionType.power)
    );

    private final ArgumentWrapper<Integer> slotA;
    private final ArgumentWrapper<Integer> slotB;
    private final SlotOperation operation;
    private final InventoryType inventoryType;
    private final Optional<PowerReference> power;

    public ChangeSlotEntityActionType(ArgumentWrapper<Integer> slotA, ArgumentWrapper<Integer> slotB,
                                      SlotOperation operation, InventoryType inventoryType,
                                      Optional<PowerReference> power) {
        this.slotA = slotA;
        this.slotB = slotB;
        this.operation = operation;
        this.inventoryType = inventoryType;
        this.power = power;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        if (entity == null) return;

        int a = slotA.parsedValue();
        int b = slotB.parsedValue();

        switch (inventoryType) {
            case INVENTORY -> handleInventory(entity, a, b, operation);
            case POWER -> handlePower(entity, a, b, operation);
        }
    }

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

    private void handlePower(Entity entity, int slotA, int slotB, SlotOperation operation) {
        if (power.isEmpty()) return;
        PowerHolderComponent.KEY.maybeGet(entity).ifPresent(phc -> {
            PowerType powerType = power.get().getNullablePowerType(entity);
            if (!(powerType instanceof InventoryPowerType inv)) return;

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

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.CHANGE_SLOT;
    }
}
