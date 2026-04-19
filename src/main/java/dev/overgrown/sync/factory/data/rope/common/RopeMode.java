package dev.overgrown.sync.factory.data.rope.common;

public enum RopeMode {
    // Toggle semantics: if the player already has a rope anchored to the
    // same target (block position or entity), detach that rope. Otherwise
    // attach a new one.
    TOGGLE,

    // Always attach a new rope, even if one already exists to the same
    // target — this is how a player ends up with multiple simultaneous
    // ropes.
    ATTACH,

    // Detach the rope (if any) that the player has to the specified
    // target. No-op when no matching rope exists. Does not touch ropes
    // pointing at other targets.
    DETACH,

    // Detach every rope the player owns, regardless of target. Useful as
    // an emergency "clear my ropes" action bound to a key.
    DETACH_ALL
}
