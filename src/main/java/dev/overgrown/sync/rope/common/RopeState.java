package dev.overgrown.sync.rope.common;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RopeState {

    // Rope ends
    public final UUID owner;
    public final Vec3d anchor;

    // Rope length
    public float maxLength;
    public double length;

    // Rope rendering
    public Identifier texture;

    // Physics state
    public double slack = 0.0;
    public int playerFlightTicks = 0;

    public RopeState(Vec3d anchor, UUID owner, double length, float maxLength, Identifier texture) {
        this.anchor = anchor;
        this.owner = owner;
        this.length = length;
        this.maxLength = maxLength;
        this.texture = texture;
    }
}