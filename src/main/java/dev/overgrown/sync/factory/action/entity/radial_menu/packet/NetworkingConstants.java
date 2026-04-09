package dev.overgrown.sync.factory.action.entity.radial_menu.packet;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenuEntry;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public class NetworkingConstants {
    public static final Identifier RADIAL_MENU_SERVER_TO_CLIENT = Sync.identifier("radial_menu_action_to_client");
    public static final Identifier RADIAL_MENU_CLIENT_TO_SERVER = Sync.identifier("radial_menu_client_to_server");

    // Helper method to create radial menu packet buffer for server-to-client
    public static PacketByteBuf createRadialMenuBuffer(List<RadialMenuEntry> entries, Identifier menuTexture) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        RadialMenuEntry.RADIAL_MENU_ENTRIES.send(buf, entries);
        if (menuTexture != null) {
            buf.writeBoolean(true);
            SerializableDataTypes.IDENTIFIER.send(buf, menuTexture);
        } else {
            buf.writeBoolean(false);
        }
        return buf;
    }
}