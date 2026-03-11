package dev.overgrown.sync.rope.client;

import dev.overgrown.sync.rope.common.RopeConstants;
import dev.overgrown.sync.rope.common.RopePackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RopeClientManager {

    private static final Map<UUID, VerletRopeState> ropes = new HashMap<>();

    public static void create(UUID owner, Vec3d anchor, double length, float maxLength, Identifier texture) {
        VerletRopeState state = new VerletRopeState(owner, anchor, length, maxLength, texture);
        ropes.put(owner, state);
    }

    public static void delete(UUID owner) {
        ropes.remove(owner);
    }

    public static void setTargetLength(UUID owner, double length) {
        VerletRopeState state = ropes.get(owner);
        if (state != null) state.targetLength = length;
    }

    public static VerletRopeState get(UUID owner) {
        return ropes.get(owner);
    }

    public static Map<UUID, VerletRopeState> getAll() {
        return ropes;
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        for (Map.Entry<UUID, VerletRopeState> entry : ropes.entrySet()) {
            UUID uuid = entry.getKey();
            VerletRopeState state = entry.getValue();

            // Find the player entity for this rope
            net.minecraft.entity.player.PlayerEntity player = client.world.getPlayerByUuid(uuid);
            if (player == null) continue;

            Vec3d attachPoint = player.getPos().add(0, player.getEyeHeight(player.getPose()) * 0.9, 0);
            state.tick(attachPoint);
        }

        // Handle local player input
        ClientPlayerEntity localPlayer = client.player;
        if (localPlayer == null) return;

        VerletRopeState localState = ropes.get(localPlayer.getUuid());
        if (localState == null) return;

        // Jump = shorten rope
        if (client.options.jumpKey.isPressed()) {
            sendChangeLength(-RopeConstants.ROPE_LENGTH_CHANGE_STEP);
        }
        // Sneak = lengthen rope
        if (client.options.sneakKey.isPressed() && !localPlayer.isOnGround()) {
            sendChangeLength(RopeConstants.ROPE_LENGTH_CHANGE_STEP);
        }

        // WASD swing
        double inputX = 0, inputZ = 0;
        if (client.options.forwardKey.isPressed()) inputZ -= 1;
        if (client.options.backKey.isPressed()) inputZ += 1;
        if (client.options.leftKey.isPressed()) inputX -= 1;
        if (client.options.rightKey.isPressed()) inputX += 1;

        if (inputX != 0 || inputZ != 0) {
            float yaw = localPlayer.getYaw();
            double rad = Math.toRadians(yaw);
            double worldX = inputX * Math.cos(rad) - inputZ * Math.sin(rad);
            double worldZ = inputX * Math.sin(rad) + inputZ * Math.cos(rad);
            sendSwing(new Vec3d(worldX, 0, worldZ));
        }
    }

    private static void sendChangeLength(double delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(client.player.getUuid());
        buf.writeDouble(delta);
        ClientPlayNetworking.send(RopePackets.ROPE_CHANGE_LENGTH, buf);
    }

    private static void sendSwing(Vec3d dir) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(dir.x);
        buf.writeDouble(dir.y);
        buf.writeDouble(dir.z);
        ClientPlayNetworking.send(RopePackets.ROPE_SWING, buf);
    }

    public static void clear() {
        ropes.clear();
    }
}