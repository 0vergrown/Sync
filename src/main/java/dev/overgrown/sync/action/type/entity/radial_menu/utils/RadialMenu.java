package dev.overgrown.sync.action.type.entity.radial_menu.utils;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.entity.radial_menu.payload.c2s.RadialMenuChoicePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class RadialMenu {

    private final List<RadialMenuEntry> entries;
    private final Optional<Identifier> menuTexture;
    private boolean buttonsInitialized = false;

    public RadialMenu(List<RadialMenuEntry> entries, Optional<Identifier> menuTexture) {
        this.entries = entries;
        this.menuTexture = menuTexture;
    }

    public void draw(MinecraftClient client, long elapsedTime) {
        positionEntries(client, elapsedTime);

        if (!buttonsInitialized) {
            entries.forEach(entry -> {
                int buttonWidth = entry.getButtonWidth();
                int buttonHeight = entry.getButtonHeight();

                Text tooltipText = entry.getTooltipText().orElse(null);
                if (tooltipText == null && !entry.getStack().isEmpty()) {
                    tooltipText = Text.literal(entry.getStack().getName().getString());
                }

                ButtonWidget button = ButtonWidget.builder(Text.empty(), w -> {
                        if (entry.getEntityAction() != null) {
                            ClientPlayNetworking.send(new RadialMenuChoicePayload(entry.getEntityAction()));
                        }
                    })
                    .position(-100, 0)
                    .size(buttonWidth, buttonHeight)
                    .tooltip(tooltipText != null ? Tooltip.of(tooltipText) : null)
                    .build();

                button.active = true;
                button.visible = true;
                entry.setButton(button);
            });
            buttonsInitialized = true;
        }

        entries.forEach(entry -> {
            ButtonWidget button = entry.getButton();
            if (button != null) {
                button.setX(Math.round(entry.getPosition().x()));
                button.setY(Math.round(entry.getPosition().y() - 1));
            }
        });
    }

    public void renderBackground(DrawContext context, MinecraftClient client) {
        menuTexture.ifPresent(tex -> {
            try {
                int centerX = client.getWindow().getScaledWidth() / 2;
                int centerY = client.getWindow().getScaledHeight() / 2;
                int textureSize = 256;
                int halfSize = textureSize / 2;

                context.drawTexture(tex, centerX - halfSize, centerY - halfSize,
                    0, 0, textureSize, textureSize, textureSize, textureSize);
            } catch (Exception e) {
                Sync.LOGGER.warn("Could not load radial menu texture: {}", tex);
            }
        });
    }

    public void renderButtons(DrawContext context, MinecraftClient client, int mouseX, int mouseY, float delta) {
        entries.forEach(entry -> {
            ButtonWidget button = entry.getButton();
            if (button == null) return;

            Identifier buttonTexture = entry.getButtonTexture().orElse(null);
            Identifier highlightButtonTexture = entry.getHighlightButtonTexture().orElse(null);

            boolean isHovered = button.isSelected();
            if (isHovered && highlightButtonTexture != null) {
                buttonTexture = highlightButtonTexture;
            }

            if (buttonTexture != null) {
                button.setAlpha(0.0f);
                button.render(context, mouseX, mouseY, delta);
                button.setAlpha(1.0f);

                try {
                    int buttonX = button.getX();
                    int buttonY = button.getY();
                    int bw = button.getWidth();
                    int bh = button.getHeight();

                    context.drawTexture(buttonTexture, buttonX, buttonY, 0, 0, bw, bh, bw, bh);
                } catch (Exception e) {
                    Sync.LOGGER.warn("Could not load button texture: {}", buttonTexture);
                }
            } else {
                button.render(context, mouseX, mouseY, delta);
            }
        });
    }

    public void renderIcons(DrawContext context, MinecraftClient client) {
        entries.forEach(entry -> {
            ButtonWidget button = entry.getButton();
            if (button == null) return;

            Identifier icon = entry.getIcon().orElse(null);
            Identifier highlightIcon = entry.getHighlightIcon().orElse(null);

            boolean isHovered = button.isSelected();
            if (isHovered && highlightIcon != null) {
                icon = highlightIcon;
            }

            if (icon != null) {
                try {
                    int buttonX = button.getX();
                    int buttonY = button.getY();
                    int bw = button.getWidth();
                    int bh = button.getHeight();

                    int iconWidth = entry.getIconWidth();
                    int iconHeight = entry.getIconHeight();

                    int iconX = buttonX + (bw - iconWidth) / 2;
                    int iconY = buttonY + (bh - iconHeight) / 2;

                    context.drawTexture(icon, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
                } catch (Exception e) {
                    Sync.LOGGER.warn("Could not load icon texture: {}", icon);
                }
            } else {
                ItemStack stack = entry.getStack();
                if (!stack.isEmpty()) {
                    int buttonX = button.getX();
                    int buttonY = button.getY();
                    int bw = button.getWidth();
                    int bh = button.getHeight();

                    int itemWidth = entry.getItemWidth();
                    int itemHeight = entry.getItemHeight();

                    int itemX = buttonX + (bw - itemWidth) / 2;
                    int itemY = buttonY + (bh - itemHeight) / 2;

                    var matrices = context.getMatrices();
                    matrices.push();

                    float scaleX = itemWidth / 16.0f;
                    float scaleY = itemHeight / 16.0f;

                    matrices.translate(itemX, itemY, 0);
                    matrices.scale(scaleX, scaleY, 1.0f);

                    context.drawItem(stack, 0, 0, 0, 100);

                    matrices.pop();
                }
            }
        });
    }

    private void positionEntries(MinecraftClient client, long elapsedTime) {
        float angleInterval = 360f / entries.size();
        for (int i = 0; i < entries.size(); i++) {
            float angle = angleInterval * i;
            Vector2f center = new Vector2f(
                client.getWindow().getScaledWidth() / 2f,
                client.getWindow().getScaledHeight() / 2f
            );

            int maxDistance = entries.get(i).getDistance() != -1
                ? entries.get(i).getDistance() : client.getWindow().getScaledHeight() / 4;
            float velocity = entries.get(i).getVelocity() != -1
                ? entries.get(i).getVelocity() : maxDistance / 3f;
            float distance = velocity * elapsedTime < maxDistance ? velocity * elapsedTime : maxDistance;

            Vector2f position = getPosFromAngle(angle, distance, center);

            int buttonWidth = entries.get(i).getButtonWidth();
            int buttonHeight = entries.get(i).getButtonHeight();
            entries.get(i).setPosition(new Vector2f(
                position.x() - buttonWidth / 2f,
                position.y() - buttonHeight / 2f));
        }
    }

    public static Vector2f getPosFromAngle(float angle, float distance, Vector2f center) {
        return new Vector2f(
            (float) (center.x() + distance * Math.cos(angle * (Math.PI / 180))),
            (float) (center.y() + distance * Math.sin(angle * (Math.PI / 180))));
    }

    public List<RadialMenuEntry> getEntries() {
        return entries;
    }

    public void resetButtons() {
        buttonsInitialized = false;
        entries.forEach(entry -> entry.setButton(null));
    }
}
