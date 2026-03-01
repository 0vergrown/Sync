package dev.overgrown.sync.factory.power.type.action_on_key_sequence.data;

import net.minecraft.entity.Entity;

import java.util.function.Consumer;

public record FunctionalKey(String key, boolean continuous, Consumer<Entity> action) {
}