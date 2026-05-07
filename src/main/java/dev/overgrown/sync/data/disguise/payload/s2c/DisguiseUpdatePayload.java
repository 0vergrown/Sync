package dev.overgrown.sync.data.disguise.payload.s2c;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.disguise.DisguiseData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.Optional;

public record DisguiseUpdatePayload(int entityNetId, Optional<DisguiseData> data) implements CustomPayload {

    public static final Id<DisguiseUpdatePayload> ID = new Id<>(Sync.identifier("disguise_update"));

    public static final PacketCodec<PacketByteBuf, DisguiseUpdatePayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, DisguiseUpdatePayload payload) {
            buf.writeInt(payload.entityNetId);
            buf.writeBoolean(payload.data.isPresent());
            payload.data.ifPresent(d -> DisguiseData.CODEC.encode(buf, d));
        }

        @Override
        public DisguiseUpdatePayload decode(PacketByteBuf buf) {
            int netId = buf.readInt();
            Optional<DisguiseData> data = buf.readBoolean()
                ? Optional.of(DisguiseData.CODEC.decode(buf))
                : Optional.empty();
            return new DisguiseUpdatePayload(netId, data);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
