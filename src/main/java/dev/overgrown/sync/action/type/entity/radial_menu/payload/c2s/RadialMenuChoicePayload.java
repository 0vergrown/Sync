package dev.overgrown.sync.action.type.entity.radial_menu.payload.c2s;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.action.EntityAction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record RadialMenuChoicePayload(EntityAction action) implements CustomPayload {

    public static final Id<RadialMenuChoicePayload> ID = new Id<>(Sync.identifier("radial_menu_client_to_server"));

    public static final PacketCodec<RegistryByteBuf, RadialMenuChoicePayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(RegistryByteBuf buf, RadialMenuChoicePayload payload) {
            EntityAction.DATA_TYPE.send(buf, payload.action);
        }

        @Override
        public RadialMenuChoicePayload decode(RegistryByteBuf buf) {
            return new RadialMenuChoicePayload(EntityAction.DATA_TYPE.receive(buf));
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
