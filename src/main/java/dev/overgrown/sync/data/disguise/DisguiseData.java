package dev.overgrown.sync.data.disguise;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Holds all information needed to represent a disguise (server + client shared).
 */
public class DisguiseData {

    private final Identifier targetEntityTypeId;
    private final int targetEntityNetId;
    @Nullable
    private final UUID targetPlayerUuid;
    @Nullable
    private final NbtCompound targetNbt;

    public DisguiseData(Identifier targetEntityTypeId,
                        int targetEntityNetId,
                        @Nullable UUID targetPlayerUuid,
                        @Nullable NbtCompound targetNbt) {
        this.targetEntityTypeId = targetEntityTypeId;
        this.targetEntityNetId = targetEntityNetId;
        this.targetPlayerUuid = targetPlayerUuid;
        this.targetNbt = targetNbt;
    }

    public Identifier getTargetEntityTypeId() {
        return targetEntityTypeId;
    }

    public int getTargetEntityNetId() {
        return targetEntityNetId;
    }

    @Nullable
    public UUID getTargetPlayerUuid() {
        return targetPlayerUuid;
    }

    @Nullable
    public NbtCompound getTargetNbt() {
        return targetNbt;
    }

    public boolean isPlayerDisguise() {
        return targetPlayerUuid != null;
    }

    public static final PacketCodec<PacketByteBuf, DisguiseData> CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, DisguiseData data) {
            buf.writeIdentifier(data.targetEntityTypeId);
            buf.writeInt(data.targetEntityNetId);
            buf.writeBoolean(data.targetPlayerUuid != null);
            if (data.targetPlayerUuid != null) {
                buf.writeUuid(data.targetPlayerUuid);
            }
            buf.writeBoolean(data.targetNbt != null);
            if (data.targetNbt != null) {
                buf.writeNbt(data.targetNbt);
            }
        }

        @Override
        public DisguiseData decode(PacketByteBuf buf) {
            Identifier typeId = buf.readIdentifier();
            int netId = buf.readInt();
            UUID playerUuid = buf.readBoolean() ? buf.readUuid() : null;
            NbtCompound nbt = buf.readBoolean() ? buf.readNbt() : null;
            return new DisguiseData(typeId, netId, playerUuid, nbt);
        }
    };
}
