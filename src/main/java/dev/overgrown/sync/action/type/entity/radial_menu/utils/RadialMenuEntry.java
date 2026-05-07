package dev.overgrown.sync.action.type.entity.radial_menu.utils;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RadialMenuEntry {

    private final ItemStack stack;
    private final Optional<Identifier> buttonTexture;
    private final Optional<Identifier> icon;
    private final Optional<Identifier> highlightIcon;
    private final Optional<Identifier> highlightButtonTexture;
    private EntityAction action;
    private Optional<EntityCondition> condition;
    private Vector2f position;
    private final int distance;
    private final int velocity;
    private final Optional<Text> tooltipText;
    private final int buttonWidth;
    private final int buttonHeight;
    private final int iconWidth;
    private final int iconHeight;
    private final int itemWidth;
    private final int itemHeight;

    @Environment(EnvType.CLIENT)
    private ButtonWidget button;

    public RadialMenuEntry(ItemStack stack, Optional<Identifier> buttonTexture, Optional<Identifier> icon,
                           Optional<Identifier> highlightIcon, Optional<Identifier> highlightButtonTexture,
                           EntityAction action, Optional<EntityCondition> condition,
                           int distance, int velocity, Optional<Text> tooltipText,
                           int buttonWidth, int buttonHeight, int iconWidth, int iconHeight,
                           int itemWidth, int itemHeight) {
        this.stack = stack;
        this.buttonTexture = buttonTexture;
        this.icon = icon;
        this.highlightIcon = highlightIcon;
        this.highlightButtonTexture = highlightButtonTexture;
        this.action = action;
        this.condition = condition;
        this.position = new Vector2f(-100f, 0f);
        this.distance = distance;
        this.velocity = velocity;
        this.tooltipText = tooltipText;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
    }

    public ItemStack getStack() { return stack; }
    public Optional<Identifier> getButtonTexture() { return buttonTexture; }
    public Optional<Identifier> getIcon() { return icon; }
    public Optional<Identifier> getHighlightIcon() { return highlightIcon; }
    public Optional<Identifier> getHighlightButtonTexture() { return highlightButtonTexture; }
    public EntityAction getEntityAction() { return action; }
    public void setEntityAction(EntityAction action) { this.action = action; }
    public Optional<EntityCondition> getCondition() { return condition; }
    public void setCondition(Optional<EntityCondition> condition) { this.condition = condition; }
    public Vector2f getPosition() { return position; }
    public void setPosition(Vector2f position) { this.position = position; }
    public int getDistance() { return distance; }
    public int getVelocity() { return velocity; }
    public Optional<Text> getTooltipText() { return tooltipText; }
    public int getButtonWidth() { return buttonWidth; }
    public int getButtonHeight() { return buttonHeight; }
    public int getIconWidth() { return iconWidth; }
    public int getIconHeight() { return iconHeight; }
    public int getItemWidth() { return itemWidth; }
    public int getItemHeight() { return itemHeight; }

    @Nullable
    public ButtonWidget getButton() { return button; }
    public void setButton(@Nullable ButtonWidget button) { this.button = button; }

    public static final SerializableDataType<RadialMenuEntry> RADIAL_MENU_ENTRY = SerializableDataType.compound(
        new SerializableData()
            .add("item", SerializableDataTypes.ITEM_STACK, ItemStack.EMPTY)
            .add("button_texture", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("icon", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("highlight_icon_texture", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("highlight_button_texture", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("entity_action", EntityAction.DATA_TYPE)
            .add("condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("distance", SerializableDataTypes.INT, -1)
            .add("velocity", SerializableDataTypes.INT, -1)
            .add("tooltip", SerializableDataTypes.TEXT.optional(), Optional.empty())
            .add("button_width", SerializableDataTypes.INT, 16)
            .add("button_height", SerializableDataTypes.INT, 20)
            .add("icon_width", SerializableDataTypes.INT, 16)
            .add("icon_height", SerializableDataTypes.INT, 16)
            .add("item_width", SerializableDataTypes.INT, 16)
            .add("item_height", SerializableDataTypes.INT, 16),
        data -> new RadialMenuEntry(
            data.get("item"),
            data.get("button_texture"),
            data.get("icon"),
            data.get("highlight_icon_texture"),
            data.get("highlight_button_texture"),
            data.get("entity_action"),
            data.get("condition"),
            data.get("distance"),
            data.get("velocity"),
            data.get("tooltip"),
            data.get("button_width"),
            data.get("button_height"),
            data.get("icon_width"),
            data.get("icon_height"),
            data.get("item_width"),
            data.get("item_height")
        ),
        (inst, sd) -> sd.instance()
            .set("item", inst.getStack())
            .set("button_texture", inst.getButtonTexture())
            .set("icon", inst.getIcon())
            .set("highlight_icon_texture", inst.getHighlightIcon())
            .set("highlight_button_texture", inst.getHighlightButtonTexture())
            .set("entity_action", inst.getEntityAction())
            .set("condition", inst.getCondition())
            .set("distance", inst.getDistance())
            .set("velocity", inst.getVelocity())
            .set("tooltip", inst.getTooltipText())
            .set("button_width", inst.getButtonWidth())
            .set("button_height", inst.getButtonHeight())
            .set("icon_width", inst.getIconWidth())
            .set("icon_height", inst.getIconHeight())
            .set("item_width", inst.getItemWidth())
            .set("item_height", inst.getItemHeight())
    );

    public static final SerializableDataType<List<RadialMenuEntry>> RADIAL_MENU_ENTRIES = RADIAL_MENU_ENTRY.list();
}
