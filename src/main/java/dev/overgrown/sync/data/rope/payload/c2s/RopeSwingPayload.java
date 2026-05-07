package dev.overgrown.sync.data.rope.payload.c2s;

import dev.overgrown.sync.Sync;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

public record RopeSwingPayload(Vec3d inputDir) implements CustomPayload {

    public static final Id<RopeSwingPayload> ID = new Id<>(Sync.identifier("rope_swing"));

    public static final PacketCodec<PacketByteBuf, RopeSwingPayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, RopeSwingPayload payload) {
            buf.writeVec3d(payload.inputDir);
        }

        @Override
        public RopeSwingPayload decode(PacketByteBuf buf) {
            return new RopeSwingPayload(buf.readVec3d());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
