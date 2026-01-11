package dev.overgrown.sync.factory.action.entity.radial_menu.client;

import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenu;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenuEntry;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RadialMenuClient {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.RADIAL_MENU_SERVER_TO_CLIENT, (client, handler, buf, responseSender) -> {
            List<RadialMenuEntry> list = RadialMenuEntry.RADIAL_MENU_ENTRIES.receive(buf);
            final Identifier menuTexture;
            if (buf.readBoolean()) {
                menuTexture = SerializableDataTypes.IDENTIFIER.receive(buf);
            } else {
                menuTexture = null;
            }
            client.execute(() -> {
                RadialMenuScreen radialMenuScreen = new RadialMenuScreen(new RadialMenu(list, menuTexture));
                client.setScreen(radialMenuScreen);
            });
        });
    }
}