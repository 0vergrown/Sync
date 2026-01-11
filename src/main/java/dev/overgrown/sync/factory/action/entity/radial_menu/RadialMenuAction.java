package dev.overgrown.sync.factory.action.entity.radial_menu;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenuEntry;
import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class RadialMenuAction {

    @SuppressWarnings("unchecked")
    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        // Get the entries and filter by condition if present
        List<RadialMenuEntry> entries = (List<RadialMenuEntry>) data.get("entries");
        List<RadialMenuEntry> filteredEntries = entries.stream()
                .filter(entry -> entry.getCondition() == null || entry.getCondition().test(entity))
                .collect(Collectors.toList());

        if (filteredEntries.isEmpty()) {
            return;
        }

        // Get the optional menu texture
        Identifier menuTexture = data.get("sprite_location");

        // Send the radial menu data to the client
        PacketByteBuf buf = NetworkingConstants.createRadialMenuBuffer(filteredEntries, menuTexture);
        ServerPlayNetworking.send(player, NetworkingConstants.RADIAL_MENU_SERVER_TO_CLIENT, buf);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("radial_menu"),
                new SerializableData()
                        .add("entries", RadialMenuEntry.RADIAL_MENU_ENTRIES)
                        .add("sprite_location", SerializableDataTypes.IDENTIFIER, null),
                RadialMenuAction::action
        );
    }
}