package dev.overgrown.sync.factory.action.entity.radial_menu.utils;

import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;

import java.util.List;

public class RadialMenu {
    private List<RadialMenuEntry> entries;
    private boolean buttonsInitialized = false;

    public RadialMenu(List<RadialMenuEntry> entries) {
        if (entries != null && entries.size() == 0) return;
        this.entries = entries;
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
                                        PacketByteBuf buf = PacketByteBufs.create();
                                        radialMenuEntry.getEntityAction().write(buf);
                                        ClientPlayNetworking.send(NetworkingConstants.RADIAL_MENU_CLIENT_TO_SERVER, buf);
                                        radialMenuEntry.setEntityAction(null);
                                    }
                                }))
                        .position(-100, 0)
                        .size(16, 20)
                        .tooltip(Tooltip.of(Text.literal(radialMenuEntry.getStack().getName().getString())))
                        .build();
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

    public void resetButtons() {
        buttonsInitialized = false;
        entries.forEach(entry -> entry.setButton(null));
    }
}