package dev.overgrown.sync.data.keybind.client;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.keybind.DataDrivenKeybindDefinition;
import dev.overgrown.sync.mixin.keybinding.GameOptionsAccessor;
import dev.overgrown.sync.mixin.keybinding.KeyBindingCategoryMapAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the lifecycle of data-driven {@link KeyBinding}s on the physical client.
 */
@Environment(EnvType.CLIENT)
public final class DynamicKeyBindingManager {

    private static final List<KeyBinding> SESSION_BINDINGS = new ArrayList<>();
    private static final Map<String, String> NAME_HINTS = new HashMap<>();

    private DynamicKeyBindingManager() {}

    public static void applyKeybinds(List<DataDrivenKeybindDefinition> definitions) {
        unregisterAll();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) {
            Sync.LOGGER.warn("[Sync/Keybinds] GameOptions not initialised; skipping dynamic keybind registration.");
            return;
        }

        for (DataDrivenKeybindDefinition def : definitions) {
            String translationKey = def.translationKey();

            if (isAlreadyInAllKeys(translationKey, client.options.allKeys)) {
                Sync.LOGGER.debug("[Sync/Keybinds] '{}' is already present in allKeys – skipping.", translationKey);
                continue;
            }

            InputUtil.Key defaultKey = parseKey(def.key(), def.id().toString());
            ensureCategoryExists(def.category());

            KeyBinding binding = new KeyBinding(
                translationKey,
                defaultKey.getCategory(),
                defaultKey.getCode(),
                def.category()
            );

            restoreSavedBinding(binding, new File(client.runDirectory, "options.txt"));

            KeyBinding[] current = client.options.allKeys;
            KeyBinding[] extended = Arrays.copyOf(current, current.length + 1);
            extended[current.length] = binding;
            ((GameOptionsAccessor) client.options).sync$setAllKeys(extended);

            SESSION_BINDINGS.add(binding);
            if (def.name() != null) NAME_HINTS.put(translationKey, def.name());

            Sync.LOGGER.debug("[Sync/Keybinds] Registered '{}' (default: {}, category: {}).",
                translationKey, def.key(), def.category());
        }

        KeyBinding.updateKeysByCode();

        Sync.LOGGER.info("[Sync/Keybinds] {} dynamic keybind(s) active for this session.",
            SESSION_BINDINGS.size());
    }

    public static void unregisterAll() {
        if (SESSION_BINDINGS.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            Set<KeyBinding> toRemove = Collections.newSetFromMap(new IdentityHashMap<>());
            toRemove.addAll(SESSION_BINDINGS);

            KeyBinding[] filtered = Arrays.stream(client.options.allKeys)
                .filter(kb -> !toRemove.contains(kb))
                .toArray(KeyBinding[]::new);
            ((GameOptionsAccessor) client.options).sync$setAllKeys(filtered);

            KeyBinding.updateKeysByCode();
        }

        SESSION_BINDINGS.clear();
        NAME_HINTS.clear();
        Sync.LOGGER.info("[Sync/Keybinds] All dynamic keybinds unregistered.");
    }

    public static String getNameHint(String translationKey) {
        return NAME_HINTS.get(translationKey);
    }

    public static List<KeyBinding> getSessionBindings() {
        return Collections.unmodifiableList(SESSION_BINDINGS);
    }

    private static void restoreSavedBinding(KeyBinding binding, File optionsFile) {
        if (optionsFile == null || !optionsFile.exists()) return;

        String prefix = "key_" + binding.getTranslationKey() + ":";

        try (BufferedReader reader = new BufferedReader(new FileReader(optionsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(prefix)) continue;

                String savedKeyStr = line.substring(prefix.length()).trim();
                try {
                    InputUtil.Key savedKey = InputUtil.fromTranslationKey(savedKeyStr);
                    binding.setBoundKey(savedKey);
                    Sync.LOGGER.debug("[Sync/Keybinds] Restored saved binding for '{}': {}.",
                        binding.getTranslationKey(), savedKeyStr);
                } catch (Exception e) {
                    Sync.LOGGER.warn("[Sync/Keybinds] Ignoring invalid saved binding '{}' for '{}': {}.",
                        savedKeyStr, binding.getTranslationKey(), e.getMessage());
                }
                return;
            }
        } catch (IOException e) {
            Sync.LOGGER.warn("[Sync/Keybinds] Could not read options.txt to restore binding for '{}': {}.",
                binding.getTranslationKey(), e.getMessage());
        }
    }

    private static boolean isAlreadyInAllKeys(String translationKey, KeyBinding[] allKeys) {
        for (KeyBinding kb : allKeys) {
            if (translationKey.equals(kb.getTranslationKey())) return true;
        }
        return false;
    }

    private static InputUtil.Key parseKey(String keyString, String ownerForLog) {
        try {
            return InputUtil.fromTranslationKey(keyString);
        } catch (Exception e) {
            Sync.LOGGER.warn("[Sync/Keybinds] Could not parse key '{}' for '{}': {}. Using UNKNOWN.",
                keyString, ownerForLog, e.getMessage());
            return InputUtil.UNKNOWN_KEY;
        }
    }

    private static void ensureCategoryExists(String category) {
        Map<String, Integer> categoryMap = KeyBindingCategoryMapAccessor.sync$getCategoryOrderMap();
        if (!categoryMap.containsKey(category)) {
            int next = categoryMap.values().stream().max(Integer::compareTo).orElse(0) + 1;
            categoryMap.put(category, next);
            Sync.LOGGER.debug("[Sync/Keybinds] Registered new controls category '{}'.", category);
        }
    }
}
