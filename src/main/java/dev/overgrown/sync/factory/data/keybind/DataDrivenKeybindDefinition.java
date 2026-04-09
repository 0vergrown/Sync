package dev.overgrown.sync.factory.data.keybind;

import net.minecraft.util.Identifier;

/**
 * Immutable record describing a single data-driven keybind loaded from
 * {@code data/<namespace>/keybinds/<path>.json}.
 *
 * <p>The {@link #translationKey()} method derives the {@link net.minecraft.client.option.KeyBinding}
 * translation key that powers / key_pressed conditions should reference:
 * {@code "key.<namespace>.<path>"} (slashes in the path are replaced with dots).
 */
public record DataDrivenKeybindDefinition(
        Identifier id,
        String key,       // e.g. "key.keyboard.h"
        String category,  // e.g. "key.category.my_mod"
        String name       // nullable; used as a display hint (see notes below)
) {
    /**
     * Returns the unique translation key for the {@link net.minecraft.client.option.KeyBinding}
     * that will be registered on the client.  Powers and key_pressed conditions should use
     * this string as their {@code "key"} value.
     *
     * <p>Example: a file at {@code data/mymod/keybinds/my_ability.json} produces
     * {@code "key.mymod.my_ability"}.
     */
    public String translationKey() {
        return "key." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }
}