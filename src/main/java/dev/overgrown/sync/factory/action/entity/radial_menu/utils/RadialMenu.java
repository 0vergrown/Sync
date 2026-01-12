package dev.overgrown.sync.factory.action.entity.radial_menu.utils;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import oshi.util.tuples.Pair;

import java.util.List;

public class RadialMenu {
    private List<RadialMenuEntry> entries;
    private Identifier menuTexture;
    private boolean buttonsInitialized = false;

    public RadialMenu(List<RadialMenuEntry> entries, Identifier menuTexture) {
        if (entries != null && entries.size() == 0) return;
        this.entries = entries;
        this.menuTexture = menuTexture;
    }

    @Environment(EnvType.CLIENT)
    public void draw(MinecraftClient client, long elapsedTime) {
        positionEntries(client, elapsedTime);

        // Only create buttons once
        if (!buttonsInitialized) {
            entries.forEach((radialMenuEntry -> {
                // Use custom width and height from entry
                int buttonWidth = radialMenuEntry.getButtonWidth();
                int buttonHeight = radialMenuEntry.getButtonHeight();

                // Determine tooltip text
                Text tooltipText = radialMenuEntry.getTooltipText();
                if (tooltipText == null && !radialMenuEntry.getStack().isEmpty()) {
                    tooltipText = Text.literal(radialMenuEntry.getStack().getName().getString());
                }

                ButtonWidget button = ButtonWidget.builder(
                                Text.empty(),
                                (widget -> {
                                    if (radialMenuEntry.getEntityAction() != null) {
                                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                                        radialMenuEntry.getEntityAction().write(buf);
                                        ClientPlayNetworking.send(NetworkingConstants.RADIAL_MENU_CLIENT_TO_SERVER, buf);
                                    }
                                }))
                        .position(-100, 0)
                        .size(buttonWidth, buttonHeight) // Use custom size
                        .tooltip(tooltipText != null ? Tooltip.of(tooltipText) : null)
                        .build();

                // Make button transparent so only the custom texture shows
                button.active = true;
                button.visible = true;
                radialMenuEntry.setButton(button);
            }));
            buttonsInitialized = true;
        }

        // Update button positions
        entries.forEach(radialMenuEntry -> {
            ButtonWidget button = radialMenuEntry.getButton();
            if (button != null) {
                button.setX(Math.round(radialMenuEntry.getPosition().x()));
                button.setY(Math.round(radialMenuEntry.getPosition().y() - 1));
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public void renderBackground(DrawContext context, MinecraftClient client) {
        if (menuTexture != null) {
            try {
                int centerX = client.getWindow().getScaledWidth() / 2;
                int centerY = client.getWindow().getScaledHeight() / 2;
                int textureSize = 256;
                int halfSize = textureSize / 2;

                context.drawTexture(menuTexture,
                        centerX - halfSize, centerY - halfSize,
                        0, 0, textureSize, textureSize, textureSize, textureSize);
            } catch (Exception e) {
                Sync.LOGGER.warn("Could not load radial menu texture: {}", menuTexture);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public void renderButtons(DrawContext context, MinecraftClient client, int mouseX, int mouseY, float delta) {
        entries.forEach(radialMenuEntry -> {
            ButtonWidget button = radialMenuEntry.getButton();
            if (button != null) {
                // Always render the button for tooltip handling, but make it invisible if we're drawing a custom texture
                if (radialMenuEntry.getButtonTexture() != null) {
                    // Temporarily set the button to be invisible so it doesn't render its default texture but still processes hover/click events and tooltips
                    button.setAlpha(0.0f);
                    button.render(context, mouseX, mouseY, delta);
                    button.setAlpha(1.0f); // Reset alpha

                    // Now draw the custom texture
                    try {
                        int buttonX = button.getX();
                        int buttonY = button.getY();
                        int buttonWidth = button.getWidth();
                        int buttonHeight = button.getHeight();

                        // Draw the button texture scaled to the button size
                        context.drawTexture(radialMenuEntry.getButtonTexture(),
                                buttonX, buttonY, 0, 0, buttonWidth, buttonHeight, buttonWidth, buttonHeight);
                    } catch (Exception e) {
                        Sync.LOGGER.warn("Could not load button texture: {}", radialMenuEntry.getButtonTexture());
                    }
                } else {
                    // Render default button only if no custom texture
                    button.render(context, mouseX, mouseY, delta);
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public void renderIcons(DrawContext context, MinecraftClient client) {
        entries.forEach(radialMenuEntry -> {
            ButtonWidget button = radialMenuEntry.getButton();
            if (button != null) {
                Identifier icon = radialMenuEntry.getIcon();
                if (icon != null) {
                    try {
                        int buttonX = button.getX();
                        int buttonY = button.getY();
                        int buttonWidth = button.getWidth();
                        int buttonHeight = button.getHeight();

                        // Get custom icon dimensions
                        int iconWidth = radialMenuEntry.getIconWidth();
                        int iconHeight = radialMenuEntry.getIconHeight();

                        // Center the icon in the button
                        int iconX = buttonX + (buttonWidth - iconWidth) / 2;
                        int iconY = buttonY + (buttonHeight - iconHeight) / 2;

                        // Draw the icon texture with custom size
                        context.drawTexture(icon, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
                    } catch (Exception e) {
                        Sync.LOGGER.warn("Could not load icon texture: {}", icon);
                    }
                } else {
                    // Fallback to item icon if no custom icon
                    ItemStack stack = radialMenuEntry.getStack();
                    if (!stack.isEmpty()) {
                        int buttonX = button.getX();
                        int buttonY = button.getY();
                        int buttonWidth = button.getWidth();
                        int buttonHeight = button.getHeight();

                        // Get custom item dimensions
                        int itemWidth = radialMenuEntry.getItemWidth();
                        int itemHeight = radialMenuEntry.getItemHeight();

                        // Center the item in the button
                        int itemX = buttonX + (buttonWidth - itemWidth) / 2;
                        int itemY = buttonY + (buttonHeight - itemHeight) / 2;

                        // Scale the item rendering since drawItem uses fixed 16x16 size
                        // Save the current matrix
                        var matrices = context.getMatrices();
                        matrices.push();

                        // Calculate scale factors
                        float scaleX = itemWidth / 16.0f;
                        float scaleY = itemHeight / 16.0f;

                        // Translate to the item position
                        matrices.translate(itemX, itemY, 0);

                        // Scale to the desired size
                        matrices.scale(scaleX, scaleY, 1.0f);

                        // Draw the item at scaled coordinates (0,0 since we translated)
                        context.drawItem(stack, 0, 0, 0, 100);

                        // Restore the matrix
                        matrices.pop();
                    }
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

            int maxDistance = entries.get(i).getDistance() != -1 ? entries.get(i).getDistance() : client.getWindow().getScaledHeight() / 4;
            float velocity = entries.get(i).getVelocity() != -1 ? entries.get(i).getVelocity() : maxDistance / 3f;
            float distance = velocity * elapsedTime < maxDistance ? velocity * elapsedTime : maxDistance;

            Vector2f position = getPosFromAngle(angle, distance, center);

            // Adjust position based on button size
            int buttonWidth = entries.get(i).getButtonWidth();
            int buttonHeight = entries.get(i).getButtonHeight();
            entries.get(i).setPosition(new Vector2f(position.x() - buttonWidth / 2f, position.y() - buttonHeight / 2f));
        }
    }

    public static Vector2f getPosFromAngle(float angle, float distance, Vector2f center) {
        return new Vector2f((float) (center.x() + distance * Math.cos(angle * (Math.PI / 180))), (float) (center.y() + distance * Math.sin(angle * (Math.PI / 180))));
    }

    public static double getAngleFromPos(Pair<Double, Double> position, Pair<Double, Double> center) {
        double deltaY = center.getB() - position.getB();
        double deltaX = center.getA() - position.getA();
        double angleInRadians = Math.atan2(deltaY, deltaX);
        double angleInDegrees = angleInRadians * (180 / Math.PI);
        if (angleInDegrees < 0)
            angleInDegrees += 360;
        return angleInDegrees;
    }

    public static Pair<Double, Double> getMousePosFromCenter(MinecraftClient client) {
        double x0 = client.getWindow().getWidth() / 2F;
        double y0 = client.getWindow().getHeight() / 2F;
        return new Pair<>(client.mouse.getX() - x0, y0 - client.mouse.getY());
    }

    public List<RadialMenuEntry> getEntries() {
        return entries;
    }

    public Identifier getMenuTexture() {
        return menuTexture;
    }

    public void resetButtons() {
        buttonsInitialized = false;
        entries.forEach(entry -> entry.setButton(null));
    }
}