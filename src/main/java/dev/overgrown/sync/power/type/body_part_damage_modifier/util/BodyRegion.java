package dev.overgrown.sync.power.type.body_part_damage_modifier.util;

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

    public boolean contains(double xNorm, double yNorm, double zNorm) {
        return xNorm >= minX && xNorm <= maxX
            && yNorm >= minY && yNorm <= maxY
            && zNorm >= minZ && zNorm <= maxZ;
    }

    public static final BodyRegion ANY = new BodyRegion(-1, 1, 0, 1, -1, 1);
    public static final BodyRegion HEAD = new BodyRegion(-1, 1, 0.88, 1.0, -1, 1);
    public static final BodyRegion TORSO = new BodyRegion(-0.80, 0.80, 0.50, 0.88, -1, 1);
    public static final BodyRegion LEFT_ARM = new BodyRegion(0.80, 1.0, 0.60, 0.88, -1, 1);
    public static final BodyRegion RIGHT_ARM = new BodyRegion(-1.0, -0.80, 0.60, 0.88, -1, 1);
    public static final BodyRegion LEGS = new BodyRegion(-1, 1, 0.18, 0.50, -1, 1);
    public static final BodyRegion FEET = new BodyRegion(-1, 1, 0.0, 0.18, -1, 1);
    public static final BodyRegion ACHILLES_HEEL = new BodyRegion(-0.35, 0.35, 0.0, 0.12, 0.30, 1.0);
    public static final BodyRegion CHEST = new BodyRegion(-0.60, 0.60, 0.70, 0.88, -1.0, 0.0);
    public static final BodyRegion BACK = new BodyRegion(-0.60, 0.60, 0.50, 0.88, 0.0, 1.0);

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
