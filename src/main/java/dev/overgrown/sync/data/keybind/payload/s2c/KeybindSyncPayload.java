package dev.overgrown.sync.data.keybind.payload.s2c;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.keybind.DataDrivenKeybindDefinition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record KeybindSyncPayload(List<DataDrivenKeybindDefinition> definitions) implements CustomPayload {

    public static final Id<KeybindSyncPayload> ID = new Id<>(Sync.identifier("keybind_sync"));

    public static final PacketCodec<PacketByteBuf, KeybindSyncPayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, KeybindSyncPayload payload) {
            buf.writeInt(payload.definitions.size());
            for (DataDrivenKeybindDefinition def : payload.definitions) {
                buf.writeIdentifier(def.id());
                buf.writeString(def.key());
                buf.writeString(def.category());
                buf.writeBoolean(def.name() != null);
                if (def.name() != null) {
                    buf.writeString(def.name());
                }
            }
        }

        @Override
        public KeybindSyncPayload decode(PacketByteBuf buf) {
            int count = buf.readInt();
            List<DataDrivenKeybindDefinition> defs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                var id = buf.readIdentifier();
                String key = buf.readString();
                String category = buf.readString();
                String name = buf.readBoolean() ? buf.readString() : null;
                defs.add(new DataDrivenKeybindDefinition(id, key, category, name));
            }
            return new KeybindSyncPayload(defs);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
