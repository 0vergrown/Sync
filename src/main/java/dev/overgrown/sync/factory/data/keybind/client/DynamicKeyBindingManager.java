package dev.overgrown.sync.factory.data.keybind.client;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.keybind.DataDrivenKeybindDefinition;
import dev.overgrown.sync.mixin.keybinding.GameOptionsAccessor;
import dev.overgrown.sync.mixin.keybinding.KeyBindingCategoryMapAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Manages the lifecycle of data-driven {@link KeyBinding}s on the physical client.
 *
 * <p><b>Registration</b> ({@link #applyKeybinds}): called when the client receives
 * the {@code KEYBIND_SYNC} packet from the server.  For each definition the manager:
 * <ol>
 *   <li>Derives a unique translation key ({@code "key.<ns>.<path>"}).</li>
 *   <li>Skips duplicates already present in {@code options.allKeys}.</li>
 *   <li>Ensures the category exists in {@code KeyBinding.CATEGORY_ORDER_MAP}.</li>
 *   <li>Constructs a {@link KeyBinding} and appends it to {@code options.allKeys}.</li>
 * </ol>
 */
public final class DynamicKeyBindingManager {

    /** All {@link KeyBinding}s registered in the current server session. */
    private static final List<KeyBinding> SESSION_BINDINGS = new ArrayList<>();

    /**
     * Optional name hint keyed by translation key.  Not used by the vanilla controls
     * screen but available for other systems.
     */
    private static final Map<String, String> NAME_HINTS = new HashMap<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Registers all supplied keybind definitions for the current server session.
     * Any previously registered session bindings are removed first.
     * Must be called on the client thread.
     */
    public static void applyKeybinds(List<DataDrivenKeybindDefinition> definitions) {
        unregisterAll();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) {
            Sync.LOGGER.warn("[Sync/Keybinds] GameOptions not initialised; skipping dynamic keybind registration.");
            return;
        }

        for (DataDrivenKeybindDefinition def : definitions) {
            String translationKey = def.translationKey();

            // Duplicate guard
            if (isAlreadyInAllKeys(translationKey, client.options.allKeys)) {
                Sync.LOGGER.debug("[Sync/Keybinds] '{}' is already present in allKeys – skipping.", translationKey);
                continue;
            }

            // Parse default key
            InputUtil.Key defaultKey = parseKey(def.key(), def.id().toString());

            // Make sure the category exists in KeyBinding's internal map
            ensureCategoryExists(def.category());

            // Create the KeyBinding
            KeyBinding binding = new KeyBinding(
                    translationKey,
                    defaultKey.getCategory(),
                    defaultKey.getCode(),
                    def.category()
            );

            // Restore any binding the player saved from a previous session before
            // we expose the key to the Controls screen or the key-tracking loop.
            restoreSavedBinding(binding, new java.io.File(client.runDirectory, "options.txt"));

            // Append to allKeys (field is final and is written via mixin accessor).
            KeyBinding[] current  = client.options.allKeys;
            KeyBinding[] extended = Arrays.copyOf(current, current.length + 1);
            extended[current.length] = binding;
            ((GameOptionsAccessor) client.options).sync$setAllKeys(extended);

            SESSION_BINDINGS.add(binding);
            if (def.name() != null) NAME_HINTS.put(translationKey, def.name());

            Sync.LOGGER.debug("[Sync/Keybinds] Registered '{}' (default: {}, category: {}).",
                    translationKey, def.key(), def.category());
        }

        // Rebuild the key to binding lookup map so restored bindings are live.
        KeyBinding.updateKeysByCode();

        Sync.LOGGER.info("[Sync/Keybinds] {} dynamic keybind(s) active for this session.",
                SESSION_BINDINGS.size());
    }

    /**
     * Removes all session keybinds from {@code allKeys} and clears tracking state.
     * Must be called on the client thread.
     */
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

    /**
     * Returns the optional display-name hint for a given translation key,
     * or {@code null} if none was supplied in the JSON.
     */
    public static String getNameHint(String translationKey) {
        return NAME_HINTS.get(translationKey);
    }

    /** Read-only view of the currently active session bindings. */
    public static List<KeyBinding> getSessionBindings() {
        return Collections.unmodifiableList(SESSION_BINDINGS);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------
    /**
     * Scans {@code optionsFile} for a line of the form
     * {@code key_<translationKey>:<savedKey>} and, if found, applies the saved
     * binding to {@code binding} via {@link KeyBinding#setBoundKey}.
     *
     * <p>This mirrors what {@code GameOptions.load()} does for built-in keybinds,
     * allowing player rebinding to survive across sessions even though these keybinds
     * are created after {@code GameOptions} has finished initializing.
     */
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

    /**
     * Parses an {@link InputUtil.Key} from a GLFW translation-key string such as
     * {@code "key.keyboard.h"} or {@code "key.mouse.left"}.
     * Falls back to {@link InputUtil#UNKNOWN_KEY} on failure.
     */
    private static InputUtil.Key parseKey(String keyString, String ownerForLog) {
        try {
            return InputUtil.fromTranslationKey(keyString);
        } catch (Exception e) {
            Sync.LOGGER.warn("[Sync/Keybinds] Could not parse key '{}' for '{}': {}. Using UNKNOWN.",
                    keyString, ownerForLog, e.getMessage());
            return InputUtil.UNKNOWN_KEY;
        }
    }

    /**
     * Inserts {@code category} into {@code KeyBinding.CATEGORY_ORDER_MAP} when it is
     * not already present, using the next available ordinal.  This is required for
     * the vanilla Controls screen to correctly group and display our bindings.
     */
    private static void ensureCategoryExists(String category) {
        Map<String, Integer> categoryMap = KeyBindingCategoryMapAccessor.sync$getCategoryOrderMap();
        if (!categoryMap.containsKey(category)) {
            int next = categoryMap.values().stream().max(Integer::compareTo).orElse(0) + 1;
            categoryMap.put(category, next);
            Sync.LOGGER.debug("[Sync/Keybinds] Registered new controls category '{}'.", category);
        }
    }

    private DynamicKeyBindingManager() {}
}