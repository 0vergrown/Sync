package dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils;

/**
 * Logical body regions used by the body_part_damage_modifier power.
 * Mapping mirrors the BHS projectile hit classifier but is independent of BHS.
 * <p>
 * yNorm bands (0 = feet, 1 = head):
 *   FEET   0.00 – 0.18
 *   LEGS   0.18 – 0.50
 *   TORSO  0.50 – 0.88  (arms break out at |xNorm| > 0.80 in the 0.60-0.88 band)
 *   HEAD   0.88 – 1.00
 */
public enum BodyRegion {
    HEAD,
    TORSO,
    LEFT_ARM,
    RIGHT_ARM,
    LEGS, // covers both legs for simplicity
    FEET, // covers both feet
    ANY; // matches all regions (wildcard)

    /** Classify a normalized hit position into a BodyRegion. */
    public static BodyRegion classify(double xNorm, double yNorm) {
        if (yNorm < 0.18) return FEET;
        if (yNorm < 0.50) return LEGS;
        if (yNorm < 0.88) {
            if (yNorm >= 0.60 && Math.abs(xNorm) > 0.80) {
                return xNorm >= 0 ? LEFT_ARM : RIGHT_ARM;
            }
            return TORSO;
        }
        return HEAD;
    }
}