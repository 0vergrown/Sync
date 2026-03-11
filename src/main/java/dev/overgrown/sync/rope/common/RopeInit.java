package dev.overgrown.sync.rope.common;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RopeInit {

    public static void init() {
        // Server tick: physics for all players with ropes
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                RopeManager.tick(player);
            }
        });

        // C2S: player requesting rope length change (jump/sneak)
        ServerPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_CHANGE_LENGTH,
                (server, player, handler, buf, responseSender) -> {
                    UUID uuid = buf.readUuid();
                    double delta = buf.readDouble();
                    server.execute(() -> {
                        if (player.getUuid().equals(uuid)) {
                            RopeManager.handleChangeLength(player, delta);
                        }
                    });
                });

        // C2S: player swing input (WASD while attached)
        ServerPlayNetworking.registerGlobalReceiver(RopePackets.ROPE_SWING,
                (server, player, handler, buf, responseSender) -> {
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Vec3d inputDir = new Vec3d(x, y, z);
                    server.execute(() -> RopeManager.handleSwing(player, inputDir));
                });
    }
}