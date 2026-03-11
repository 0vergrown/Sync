package dev.overgrown.sync.rope.client;

import net.minecraft.util.math.Vec3d;

public class RopePoint {
    public Vec3d pos;
    public Vec3d prevPos;

    public RopePoint(Vec3d pos) {
        this.pos = pos;
        this.prevPos = pos;
    }
}