package dev.overgrown.sync.data.rope.common;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RopeState {

    public static final int NO_ANCHOR_ENTITY = -1;

    public final UUID ropeId;
    public final UUID owner;
    public Vec3d anchor;
    public int anchorEntityId = NO_ANCHOR_ENTITY;
    public boolean leash;
    public float maxLength;
    public double length;
    public Identifier texture;
    public int playerFlightTicks = 0;

    public RopeState(UUID ropeId, Vec3d anchor, UUID owner, double length, float maxLength, Identifier texture) {
        this.ropeId = ropeId;
        this.anchor = anchor;
        this.owner = owner;
        this.length = length;
        this.maxLength = maxLength;
        this.texture = texture;
    }
}
