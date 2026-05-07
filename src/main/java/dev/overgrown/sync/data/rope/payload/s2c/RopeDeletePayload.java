package dev.overgrown.sync.data.rope.payload.s2c;

import dev.overgrown.sync.Sync;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record RopeDeletePayload(UUID ropeId) implements CustomPayload {

    public static final Id<RopeDeletePayload> ID = new Id<>(Sync.identifier("rope_delete"));

    public static final PacketCodec<PacketByteBuf, RopeDeletePayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, RopeDeletePayload payload) {
            buf.writeUuid(payload.ropeId);
        }

        @Override
        public RopeDeletePayload decode(PacketByteBuf buf) {
            return new RopeDeletePayload(buf.readUuid());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
