package dev.overgrown.sync.factory.data.rope.common;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RopeState {

    public static final int NO_ANCHOR_ENTITY = -1;

    // Per-rope unique id (independent of the owner). One owner can have many
    // ropes; this is the key that uniquely identifies each rope across server
    // and client.
    public final UUID ropeId;

    // Rope ends
    public final UUID owner;
    public Vec3d anchor;

    // Optional entity anchor (network id). NO_ANCHOR_ENTITY when the rope is
    // anchored to a static world-space point (the original "attach to block"
    // behavior).
    public int anchorEntityId = NO_ANCHOR_ENTITY;

    // When true, the leash constraint is applied to the anchor entity instead
    // of the rope owner — the owner acts as a moving knot and pulls the anchor
    // along, matching vanilla lead behavior.
    public boolean leash;

    // Rope length
    public float maxLength;
    public double length;

    // Rope rendering
    public Identifier texture;

    // Physics state
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
