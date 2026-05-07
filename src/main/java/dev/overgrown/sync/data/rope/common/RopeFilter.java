package dev.overgrown.sync.data.rope.common;

public enum RopeFilter {
    ANY,
    BLOCK,
    ENTITY,
    LEASH,
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
