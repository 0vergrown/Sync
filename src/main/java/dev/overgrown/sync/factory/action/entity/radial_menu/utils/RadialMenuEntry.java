package dev.overgrown.sync.factory.action.entity.radial_menu.utils;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class RadialMenuEntry {
    private final ItemStack stack;
    private final Identifier buttonTexture;
    private final Identifier icon;
    private ActionFactory<Entity>.Instance action;
    private ConditionFactory<Entity>.Instance condition;
    private Vector2f position;
    private final int distance;
    private final int velocity;
    private final Text tooltipText;
    private final int buttonWidth;
    private final int buttonHeight;

    @Environment(EnvType.CLIENT)
    private ButtonWidget button;

    public RadialMenuEntry(ItemStack stack, Identifier buttonTexture, Identifier icon,
                           ActionFactory<Entity>.Instance action,
                           ConditionFactory<Entity>.Instance condition, int distance, int velocity,
                           Text tooltipText, int buttonWidth, int buttonHeight) {
        this.stack = stack;
        this.buttonTexture = buttonTexture;
        this.icon = icon;
        this.action = action;
        this.condition = condition;
        position = new Vector2f(-100f, 0f);
        this.distance = distance;
        this.velocity = velocity;
        this.tooltipText = tooltipText;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Identifier getButtonTexture() {
        return buttonTexture;
    }

    public Identifier getIcon() {
        return icon;
    }

    public ActionFactory<Entity>.Instance getEntityAction() {
        return action;
    }
    public void setEntityAction(ActionFactory<Entity>.Instance action) {
        this.action = action;
    }

    public ConditionFactory<Entity>.Instance getCondition() {
        return condition;
    }
    public void setCondition(ConditionFactory<Entity>.Instance condition) {
        this.condition = condition;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public int getDistance() {
        return distance;
    }

    public int getVelocity() {
        return velocity;
    }

    public Text getTooltipText() {
        return tooltipText;
    }

    public int getButtonWidth() {
        return buttonWidth;
    }

    public int getButtonHeight() {
        return buttonHeight;
    }

    public ButtonWidget getButton() {
        return button;
    }

    public void setButton(ButtonWidget button) {
        this.button = button;
    }

    public static final SerializableDataType<RadialMenuEntry> RADIAL_MENU_ENTRY = SerializableDataType.compound(
            RadialMenuEntry.class,
            new SerializableData()
                    .add("item", SerializableDataTypes.ITEM_STACK, ItemStack.EMPTY)
                    .add("button_texture", SerializableDataTypes.IDENTIFIER, null)
                    .add("icon", SerializableDataTypes.IDENTIFIER, null)
                    .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                    .add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
                    .add("distance", SerializableDataTypes.INT, -1)
                    .add("velocity", SerializableDataTypes.INT, -1)
                    .add("tooltip", SerializableDataTypes.TEXT, null)
                    .add("width", SerializableDataTypes.INT, 16)
                    .add("height", SerializableDataTypes.INT, 20),
            data -> new RadialMenuEntry(
                    data.get("item"),
                    data.get("button_texture"),
                    data.get("icon"),
                    data.get("entity_action"),
                    data.get("condition"),
                    data.get("distance"),
                    data.get("velocity"),
                    data.get("tooltip"),
                    data.get("width"),
                    data.get("height")
            ),
            (data, inst) -> {
                SerializableData.Instance dataInst = data.new Instance();
                dataInst.set("item", inst.getStack());
                dataInst.set("button_texture", inst.getButtonTexture());
                dataInst.set("icon", inst.getIcon());
                dataInst.set("entity_action", inst.getEntityAction());
                dataInst.set("condition", inst.getCondition());
                dataInst.set("distance", inst.getDistance());
                dataInst.set("velocity", inst.getVelocity());
                dataInst.set("tooltip", inst.getTooltipText());
                dataInst.set("width", inst.getButtonWidth());
                dataInst.set("height", inst.getButtonHeight());
                return dataInst;
            });

    public static final SerializableDataType<List<RadialMenuEntry>> RADIAL_MENU_ENTRIES =
            SerializableDataType.list(RADIAL_MENU_ENTRY);
}