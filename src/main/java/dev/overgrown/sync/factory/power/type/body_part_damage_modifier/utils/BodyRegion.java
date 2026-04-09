package dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils;

/**
 * An axis-aligned box in normalized hit-space that defines one body zone.
 * <p>
 * Coordinate conventions (all values -1..1 unless noted):
 *   xNorm : -1 = entity's right, +1 = entity's left
 *   yNorm :  0 = feet, 1 = top of head
 *   zNorm : -1 = front of entity, +1 = back of entity
 * <p>
 * Named presets are provided as static constants for the common regions.
 * Custom zones can be constructed directly for sub-regions like an Achilles heel.
 */
public final class BodyRegion {

    public final double minX, maxX;
    public final double minY, maxY;
    public final double minZ, maxZ;

    public BodyRegion(double minX, double maxX,
                      double minY, double maxY,
                      double minZ, double maxZ) {
        this.minX = minX; this.maxX = maxX;
        this.minY = minY; this.maxY = maxY;
        this.minZ = minZ; this.maxZ = maxZ;
    }

    /** Returns true when the normalized hit position falls inside this zone. */
    public boolean contains(double xNorm, double yNorm, double zNorm) {
        return xNorm >= minX && xNorm <= maxX
                && yNorm >= minY && yNorm <= maxY
                && zNorm >= minZ && zNorm <= maxZ;
    }

    // ------------------------------------------------------------------ //
    //  Named presets  (front/back ranges are intentionally unconstrained //
    //  so they match hits from any direction unless you narrow them)     //
    // ------------------------------------------------------------------ //

    /** Matches any hit location. */
    public static final BodyRegion ANY =
            new BodyRegion(-1, 1, 0, 1, -1, 1);

    public static final BodyRegion HEAD =
            new BodyRegion(-1, 1, 0.88, 1.0, -1, 1);

    /** Torso, excluding the arm band. */
    public static final BodyRegion TORSO =
            new BodyRegion(-0.80, 0.80, 0.50, 0.88, -1, 1);

    /** Left arm (entity's left = positive x). */
    public static final BodyRegion LEFT_ARM =
            new BodyRegion(0.80, 1.0, 0.60, 0.88, -1, 1);

    /** Right arm (entity's right = negative x). */
    public static final BodyRegion RIGHT_ARM =
            new BodyRegion(-1.0, -0.80, 0.60, 0.88, -1, 1);

    public static final BodyRegion LEGS =
            new BodyRegion(-1, 1, 0.18, 0.50, -1, 1);

    public static final BodyRegion FEET =
            new BodyRegion(-1, 1, 0.0, 0.18, -1, 1);

    // Example fine-grained presets:
    /** Back of the lower foot – the achilles heel region. */
    public static final BodyRegion ACHILLES_HEEL =
            new BodyRegion(-0.35, 0.35, 0.0, 0.12, 0.30, 1.0);

    /** Front of the upper torso / chest. */
    public static final BodyRegion CHEST =
            new BodyRegion(-0.60, 0.60, 0.70, 0.88, -1.0, 0.0);

    /** Back of the torso. */
    public static final BodyRegion BACK =
            new BodyRegion(-0.60, 0.60, 0.50, 0.88, 0.0, 1.0);

    /**
     * Resolves a preset name to a BodyRegion instance, or returns null if
     * the name is unrecognized (caller should then fall back to explicit ranges).
     */
    public static BodyRegion fromPresetName(String name) {
        return switch (name.toLowerCase()) {
            case "any" -> ANY;
            case "head" -> HEAD;
            case "torso" -> TORSO;
            case "left_arm" -> LEFT_ARM;
            case "right_arm" -> RIGHT_ARM;
            case "legs" -> LEGS;
            case "feet" -> FEET;
            case "achilles_heel" -> ACHILLES_HEEL;
            case "chest" -> CHEST;
            case "back" -> BACK;
            default -> null;
        };
    }
}