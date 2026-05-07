package dev.overgrown.sync.power.type.action_on_key_sequence.data;

import io.github.apace100.apoli.action.EntityAction;

import java.util.Optional;

public record FunctionalKey(String key, boolean continuous, Optional<EntityAction> action) {
}
