package dev.overgrown.sync.action.type.entity.radial_menu.payload.s2c;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.entity.radial_menu.utils.RadialMenuEntry;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Server-to-client packet that opens a radial menu screen with the given entries.
 *
 * <p>Note: serialization of {@link RadialMenuEntry} (which contains an EntityAction)
 * relies on the calio {@link RadialMenuEntry#RADIAL_MENU_ENTRIES}
 * {@code SerializableDataType}, accessed via {@code .send/receive} methods on a
 * legacy {@code PacketByteBuf}. Apoli 1.21 still exposes these through
 * {@code SerializableDataType.send(buf, value)} / {@code receive(buf)}.
 */
public record OpenRadialMenuPayload(List<RadialMenuEntry> entries, Optional<Identifier> menuTexture) implements CustomPayload {

    public static final Id<OpenRadialMenuPayload> ID = new Id<>(Sync.identifier("radial_menu_action_to_client"));

    public static final PacketCodec<RegistryByteBuf, OpenRadialMenuPayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(RegistryByteBuf buf, OpenRadialMenuPayload payload) {
            RadialMenuEntry.RADIAL_MENU_ENTRIES.send(buf, payload.entries);
            buf.writeBoolean(payload.menuTexture.isPresent());
            payload.menuTexture.ifPresent(id -> SerializableDataTypes.IDENTIFIER.send(buf, id));
        }

        @Override
        public OpenRadialMenuPayload decode(RegistryByteBuf buf) {
            List<RadialMenuEntry> entries = RadialMenuEntry.RADIAL_MENU_ENTRIES.receive(buf);
            Optional<Identifier> texture = buf.readBoolean()
                ? Optional.of(SerializableDataTypes.IDENTIFIER.receive(buf))
                : Optional.empty();
            return new OpenRadialMenuPayload(entries, texture);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
