package dev.overgrown.sync.mixin.keybinding;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Exposes the private static {@code CATEGORY_ORDER_MAP} field of {@link KeyBinding}
 * so that {@link dev.overgrown.sync.factory.data.keybind.client.DynamicKeyBindingManager} can
 * register new categories at any time without going through Fabric's
 * {@code KeyBindingRegistryImpl} (which would throw after {@code GameOptions} is
 * already initialized).
 */
@Mixin(KeyBinding.class)
public interface KeyBindingCategoryMapAccessor {

    @Accessor("CATEGORY_ORDER_MAP")
    static Map<String, Integer> sync$getCategoryOrderMap() {
        throw new AssertionError("Mixin not applied");
    }
}