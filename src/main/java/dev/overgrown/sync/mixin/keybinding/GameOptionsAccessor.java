package dev.overgrown.sync.mixin.keybinding;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameOptions.class)
public interface GameOptionsAccessor {

    @Mutable
    @Accessor("allKeys")
    void sync$setAllKeys(KeyBinding[] allKeys);
}
