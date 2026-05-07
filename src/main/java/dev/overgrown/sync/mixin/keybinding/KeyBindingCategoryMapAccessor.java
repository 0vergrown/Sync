package dev.overgrown.sync.mixin.keybinding;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingCategoryMapAccessor {

    @Accessor("CATEGORY_ORDER_MAP")
    static Map<String, Integer> sync$getCategoryOrderMap() {
        throw new AssertionError("Mixin not applied");
    }
}
