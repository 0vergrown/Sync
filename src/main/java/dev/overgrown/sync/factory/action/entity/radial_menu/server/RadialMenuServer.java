package dev.overgrown.sync.factory.action.entity.radial_menu.server;

import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;

public class RadialMenuServer {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.RADIAL_MENU_CLIENT_TO_SERVER, (server, player, handler, buf, responseSender) -> {
            ActionFactory<Entity>.Instance action = ApoliDataTypes.ENTITY_ACTION.receive(buf);
            server.execute(() -> {
                player.closeHandledScreen();
                action.accept(player);
            });
        });
    }
}