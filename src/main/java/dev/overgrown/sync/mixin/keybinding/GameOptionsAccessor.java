package dev.overgrown.sync.mixin.keybinding;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the {@code allKeys} field of {@link GameOptions} for writing.
 * Although {@code allKeys} is declared {@code public}, it is {@code final},
 * so direct assignment from non-mixin code is rejected by the compiler.
 * This accessor bypasses that restriction so
 * {@link dev.overgrown.sync.factory.data.keybind.client.DynamicKeyBindingManager}
 * can append / remove dynamically registered keybindings at runtime.
 */
@Mixin(GameOptions.class)
public interface GameOptionsAccessor {

    @Accessor("allKeys")
    void sync$setAllKeys(KeyBinding[] allKeys);
}