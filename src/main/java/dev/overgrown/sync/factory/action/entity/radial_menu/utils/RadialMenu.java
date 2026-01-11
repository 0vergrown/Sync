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
                        .size(16, 20)
                        .tooltip(Tooltip.of(Text.literal(radialMenuEntry.getStack().getName().getString())))
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
                // Draw custom button texture if provided
                if (radialMenuEntry.getButtonTexture() != null) {
                    try {
                        int buttonX = button.getX();
                        int buttonY = button.getY();
                        int buttonWidth = button.getWidth();
                        int buttonHeight = button.getHeight();

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
                        // Center the icon in the button
                        int iconX = buttonX + (button.getWidth() - 16) / 2;
                        int iconY = buttonY + (button.getHeight() - 16) / 2;

                        // Draw the icon texture (assuming 16x16 icon)
                        context.drawTexture(icon, iconX, iconY, 0, 0, 16, 16, 16, 16);
                    } catch (Exception e) {
                        Sync.LOGGER.warn("Could not load icon texture: {}", icon);
                    }
                } else {
                    // Fallback to item icon if no custom icon
                    ItemStack stack = radialMenuEntry.getStack();
                    if (!stack.isEmpty()) {
                        int buttonX = button.getX();
                        int buttonY = button.getY();
                        int iconX = buttonX + (button.getWidth() - 16) / 2;
                        int iconY = buttonY + (button.getHeight() - 16) / 2;

                        context.drawItem(stack, iconX, iconY, 0, 100);
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
            entries.get(i).setPosition(new Vector2f(position.x() - 8f, position.y() - 10f));
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