package dev.overgrown.sync.data.rope.payload.s2c;

import dev.overgrown.sync.Sync;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record RopeVerletLengthPayload(UUID ropeId, double length) implements CustomPayload {

    public static final Id<RopeVerletLengthPayload> ID = new Id<>(Sync.identifier("rope_verlet_length"));

    public static final PacketCodec<PacketByteBuf, RopeVerletLengthPayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, RopeVerletLengthPayload payload) {
            buf.writeUuid(payload.ropeId);
            buf.writeDouble(payload.length);
        }

        @Override
        public RopeVerletLengthPayload decode(PacketByteBuf buf) {
            return new RopeVerletLengthPayload(
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
