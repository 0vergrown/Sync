package dev.overgrown.sync.rope.common;

import dev.overgrown.sync.Sync;
import net.minecraft.util.Identifier;

public class RopePackets {

    // Server -> Client
    public static final Identifier ROPE_CREATE         = Sync.identifier("rope_create");
    public static final Identifier ROPE_DELETE         = Sync.identifier("rope_delete");
    public static final Identifier ROPE_VERLET_LENGTH  = Sync.identifier("rope_verlet_length");

    // Client -> Server
    public static final Identifier ROPE_CHANGE_LENGTH  = Sync.identifier("rope_change_length");
    public static final Identifier ROPE_SWING          = Sync.identifier("rope_swing");
}