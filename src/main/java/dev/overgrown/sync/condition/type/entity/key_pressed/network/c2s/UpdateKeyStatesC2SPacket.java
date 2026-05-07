package dev.overgrown.sync.condition.type.entity.key_pressed.network.c2s;

import dev.overgrown.sync.Sync;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.HashSet;
import java.util.Set;

public record UpdateKeyStatesC2SPacket(Set<String> pressedKeys) implements CustomPayload {

    public static final Id<UpdateKeyStatesC2SPacket> PACKET_ID = new Id<>(Sync.identifier("c2s/update_key_states"));

    public static final PacketCodec<RegistryByteBuf, UpdateKeyStatesC2SPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.collection(HashSet::new, PacketCodecs.STRING), UpdateKeyStatesC2SPacket::pressedKeys,
        UpdateKeyStatesC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
