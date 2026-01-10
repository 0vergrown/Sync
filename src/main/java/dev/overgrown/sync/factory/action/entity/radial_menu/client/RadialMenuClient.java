package dev.overgrown.sync.factory.action.entity.radial_menu.client;

import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenu;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenuEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RadialMenuClient {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.RADIAL_MENU_SERVER_TO_CLIENT, (client, handler, buf, responseSender) -> {
            List<RadialMenuEntry> list = RadialMenuEntry.RADIAL_MENU_ENTRIES.receive(buf);
            client.execute(() -> {
                RadialMenuScreen radialMenuScreen = new RadialMenuScreen(new RadialMenu(list));
                client.setScreen(radialMenuScreen);
            });
        });
    }
}