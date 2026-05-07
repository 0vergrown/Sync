package dev.overgrown.sync.data.keybind;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable record describing a single data-driven keybind loaded from
 * {@code data/<namespace>/keybinds/<path>.json}.
 */
public record DataDrivenKeybindDefinition(
    Identifier id,
    String key,
    String category,
    @Nullable String name
) {
    public String translationKey() {
        return "key." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }
}
