package dev.overgrown.sync.rope.client;

import dev.overgrown.sync.rope.common.RopePackets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RopeClientInit {

    public static void init() {
        RopeRenderer.register();

        // Client tick for physics + input
        ClientTickEvents.END_CLIENT_TICK.register(client -> RopeClientManager.tick());

        // Clear ropes on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RopeClientManager.clear());

        // S2C: rope created
        ClientPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_CREATE,
                (client, handler, buf, responseSender) -> {
                    UUID owner = buf.readUuid();
                    double ax = buf.readDouble(), ay = buf.readDouble(), az = buf.readDouble();
                    double length = buf.readDouble();
                    float maxLength = buf.readFloat();
                    Identifier texture = buf.readIdentifier();
                    Vec3d anchor = new Vec3d(ax, ay, az);
                    client.execute(() -> RopeClientManager.create(owner, anchor, length, maxLength, texture));
                });

        // S2C: rope deleted
        ClientPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_DELETE,
                (client, handler, buf, responseSender) -> {
                    UUID owner = buf.readUuid();
                    client.execute(() -> RopeClientManager.delete(owner));
                });

        // S2C: rope length updated
        ClientPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_VERLET_LENGTH,
                (client, handler, buf, responseSender) -> {
                    UUID owner = buf.readUuid();
                    double length = buf.readDouble();
                    client.execute(() -> RopeClientManager.setTargetLength(owner, length));
                });
    }
}