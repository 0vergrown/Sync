package dev.overgrown.sync.factory.data.rope.common;

public enum RopeFilter {
    // Count every rope the owner has.
    ANY,

    // Ropes anchored to a static world-space point (no anchor entity).
    BLOCK,

    // Ropes whose anchor follows an entity — includes leash-style ropes.
    ENTITY,

    // Leash-style ropes only (pull the anchor toward the owner). A subset
    // of ENTITY.
    LEASH,

    // Non-leash ropes — block anchors plus entity anchors that pull the
    // owner toward the anchor. The complement of LEASH within ANY.
    SWING;

    public boolean matches(RopeState rope) {
        return switch (this) {
            case ANY -> true;
            case BLOCK -> rope.anchorEntityId == RopeState.NO_ANCHOR_ENTITY;
            case ENTITY -> rope.anchorEntityId != RopeState.NO_ANCHOR_ENTITY;
            case LEASH -> rope.leash;
            case SWING -> !rope.leash;
        };
    }
}
