package dev.overgrown.sync.data.rope.payload.s2c;

import dev.overgrown.sync.Sync;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record RopeCreatePayload(
    UUID ropeId,
    UUID owner,
    Vec3d anchor,
    double length,
    float maxLength,
    Identifier texture,
    int anchorEntityId,
    boolean leash
) implements CustomPayload {

    public static final Id<RopeCreatePayload> ID = new Id<>(Sync.identifier("rope_create"));

    public static final PacketCodec<PacketByteBuf, RopeCreatePayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, RopeCreatePayload payload) {
            buf.writeUuid(payload.ropeId);
            buf.writeUuid(payload.owner);
            buf.writeVec3d(payload.anchor);
            buf.writeDouble(payload.length);
            buf.writeFloat(payload.maxLength);
            buf.writeIdentifier(payload.texture);
            buf.writeVarInt(payload.anchorEntityId);
            buf.writeBoolean(payload.leash);
        }

        @Override
        public RopeCreatePayload decode(PacketByteBuf buf) {
            return new RopeCreatePayload(
                buf.readUuid(),
                buf.readUuid(),
                buf.readVec3d(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readIdentifier(),
                buf.readVarInt(),
                buf.readBoolean()
            );
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
