package dev.overgrown.sync.factory.data.disguise;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
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
    private final UUID targetPlayerUuid; // non-null only when disguised as a player
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

    /** True when the disguise target is a player (skin-swap path). */
    public boolean isPlayerDisguise() {
        return targetPlayerUuid != null;
    }

    // Packet serialisation
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(targetEntityTypeId);
        buf.writeInt(targetEntityNetId);
        buf.writeBoolean(targetPlayerUuid != null);
        if (targetPlayerUuid != null) {
            buf.writeUuid(targetPlayerUuid);
        }
        buf.writeBoolean(targetNbt != null);
        if (targetNbt != null) {
            buf.writeNbt(targetNbt);
        }
    }

    public static DisguiseData read(PacketByteBuf buf) {
        Identifier typeId  = buf.readIdentifier();
        int         netId  = buf.readInt();
        UUID playerUuid    = buf.readBoolean() ? buf.readUuid() : null;
        NbtCompound nbt    = buf.readBoolean() ? buf.readNbt() : null;
        return new DisguiseData(typeId, netId, playerUuid, nbt);
    }
}