package dev.overgrown.sync.rope.common;

import net.minecraft.util.math.Vec3d;

public class RopeConstants {

    // Rope Length
    public static final float MIN_ROPE_LENGTH = 1.0f;
    public static final float GOAL_ROPE_SEGMENT_LENGTH = 0.5f;
    public static final float ROPE_LENGTH_CHANGE_STEP = 0.2f;
    public static final float SLACK_PULL_RATE_MULT = 5;

    // Player Physics Constants
    public static final float RADIAL_DAMPING = 0.85f;
    public static final float LEASH_STIFFNESS = 0.1f;
    public static final float SPRING_SCALING = 0.65f;
    public static final float SWING_BOOST = 1.08f;
    public static final float MAX_SWING_SPEED = 0.7f;
    public static final float ELYTRA_LENGTH_MOD = 5;
    public static final int ELYTRA_TIME_LIMIT = 10;

    // Rope Physics Constants
    public static final Vec3d GRAVITY = new Vec3d(0, -0.08, 0);
    public static final float ROPE_DAMPING = 0.99f;
    public static final float ROPE_STIFFNESS = 0.98f;

    // Rope Rendering
    public static final float ROPE_WIDTH = 0.1f;
}