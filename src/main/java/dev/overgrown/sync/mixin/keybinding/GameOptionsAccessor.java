package dev.overgrown.sync.mixin.keybinding;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@code GameOptions.allKeys} for writing.
 *
 * <p>{@code allKeys} is {@code public final KeyBinding[]}.  The {@code final}
 * makes direct assignment a compile-time error, but Mixin strips the final
 * modifier during class transformation so the setter below works at runtime.
 * {@link Mutable} is required to tell Mixin that stripping the final flag is
 * intentional; without it some Mixin versions refuse to generate the setter.
 */
@Mixin(GameOptions.class)
public interface GameOptionsAccessor {

    @Mutable
    @Accessor("allKeys")
    void sync$setAllKeys(KeyBinding[] allKeys);
}