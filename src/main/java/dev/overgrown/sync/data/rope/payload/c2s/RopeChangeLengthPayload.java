package dev.overgrown.sync.data.rope.payload.c2s;

import dev.overgrown.sync.Sync;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record RopeChangeLengthPayload(UUID owner, double delta) implements CustomPayload {

    public static final Id<RopeChangeLengthPayload> ID = new Id<>(Sync.identifier("rope_change_length"));

    public static final PacketCodec<PacketByteBuf, RopeChangeLengthPayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, RopeChangeLengthPayload payload) {
            buf.writeUuid(payload.owner);
            buf.writeDouble(payload.delta);
        }

        @Override
        public RopeChangeLengthPayload decode(PacketByteBuf buf) {
            return new RopeChangeLengthPayload(
                buf.readUuid(),
                buf.readDouble()
            );
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
