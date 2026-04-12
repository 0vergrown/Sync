package dev.overgrown.sync.factory.power.type.action_on_sending_message.utils;

import dev.overgrown.sync.Sync;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolves {@code #{translation.key}} placeholders in regex filter patterns by
 * expanding them into alternations of every known translation for that key
 * across all loaded languages.
 *
 * <p>For example, if the translation key {@code spell.fireball} resolves to:
 * <ul>
 *   <li>{@code en_us}: "Fireball"</li>
 *   <li>{@code es_es}: "Bola de fuego"</li>
 * </ul>
 * then the pattern {@code cast #{spell.fireball}} expands to
 * {@code cast (?i:\QFireball\E|\QBola de fuego\E)}.
 *
 * <p>The expanded group is case-insensitive so that player-typed chat
 * (often lowercase) matches translation values (often Title Case).
 * Longer translations are tried first so partial prefixes do not
 * short-circuit a longer match.
 *
 * <h3>Loading strategies</h3>
 * <ul>
 *   <li>{@link #load()} — scans mod JARs via FabricLoader; used as the initial
 *       load during mod init and as the only source on dedicated servers.</li>
 *   <li>{@link #loadFromResourceManager(ResourceManager)} — scans the client
 *       {@code ResourceManager} which has access to all language files from all
 *       resource packs and mods. Registered as a client resource reload listener
 *       so it fires before datapacks are parsed.</li>
 * </ul>
 */
public final class TranslationKeyResolver {

    /** Matches {@code #{some.translation.key}} placeholders. */
    private static final Pattern PLACEHOLDER = Pattern.compile("#\\{([^}]+)}");

    /** translation key -> every unique translated value across all loaded languages. */
    private static volatile Map<String, Set<String>> translations = Collections.emptyMap();

    private TranslationKeyResolver() {}

    // ------------------------------------------------------------------
    //  Loading: FabricLoader (common / dedicated-server fallback)
    // ------------------------------------------------------------------

    /**
     * Scans every loaded mod's {@code assets/<namespace>/lang/*.json} files and
     * indexes all translation key -> value mappings.
     * It's called once during mod initialization, before datapacks are loaded.
     *
     * <p>On a client this is later superseded by
     * {@link #loadFromResourceManager(ResourceManager)} which has access to all
     * client resources including Minecraft's full set of language files.
     */
    public static void load() {
        Map<String, Set<String>> map = new HashMap<>();
        int fileCount = 0;

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            for (Path root : mod.getRootPaths()) {
                Path assetsDir = root.resolve("assets");
                if (!Files.isDirectory(assetsDir)) continue;

                try (DirectoryStream<Path> namespaces = Files.newDirectoryStream(assetsDir)) {
                    for (Path namespace : namespaces) {
                        if (!Files.isDirectory(namespace)) continue;
                        Path langDir = namespace.resolve("lang");
                        if (!Files.isDirectory(langDir)) continue;

                        try (DirectoryStream<Path> langFiles =
                                     Files.newDirectoryStream(langDir, "*.json")) {
                            for (Path langFile : langFiles) {
                                loadFile(langFile, map);
                                fileCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    Sync.LOGGER.warn("[Sync] Failed to scan lang files for mod {}: {}",
                            mod.getMetadata().getId(), e.getMessage());
                }
            }
        }

        translations = Collections.unmodifiableMap(map);
        Sync.LOGGER.info("[Sync] Loaded {} translation key(s) from {} language file(s)",
                map.size(), fileCount);
    }

    private static void loadFile(Path file, Map<String, Set<String>> map) {
        try (InputStream in = Files.newInputStream(file)) {
            Language.load(in, (key, value) -> {
                if (!value.isEmpty()) {
                    map.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
                }
            });
        } catch (Exception e) {
            Sync.LOGGER.warn("[Sync] Failed to read language file {}: {}", file, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Loading: Client ResourceManager (comprehensive)
    // ------------------------------------------------------------------

    /**
     * Loads every {@code lang/*.json} resource visible to the given
     * {@link ResourceManager}. On the client this includes Minecraft's own
     * language files for all locales plus every loaded mod and resource pack.
     *
     * <p>This replaces any translations previously loaded by {@link #load()}.
     */
    public static void loadFromResourceManager(ResourceManager manager) {
        Map<String, Set<String>> map = new HashMap<>();
        int fileCount = 0;

        Map<Identifier, List<Resource>> allLangResources =
                manager.findAllResources("lang", id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, List<Resource>> entry : allLangResources.entrySet()) {
            for (Resource resource : entry.getValue()) {
                try (InputStream in = resource.getInputStream()) {
                    Language.load(in, (key, value) -> {
                        if (!value.isEmpty()) {
                            map.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
                        }
                    });
                    fileCount++;
                } catch (Exception e) {
                    Sync.LOGGER.warn("[Sync] Failed to load language resource {}: {}",
                            entry.getKey(), e.getMessage());
                }
            }
        }

        translations = Collections.unmodifiableMap(map);
        Sync.LOGGER.info("[Sync] Loaded {} translation key(s) from {} language resource(s) "
                + "via ResourceManager", map.size(), fileCount);
    }

    // ------------------------------------------------------------------
    //  Pattern expansion
    // ------------------------------------------------------------------

    /**
     * Expands every {@code #{translation.key}} placeholder in the given pattern.
     * Each placeholder is replaced with a case-insensitive, non-capturing group
     * that matches any known translation of that key.  Placeholders whose keys
     * are not found in any language file are left unchanged.
     *
     * @param rawPattern regex pattern potentially containing placeholders
     * @return the expanded pattern, ready for {@link Pattern#compile(String)}
     */
    public static String expandPattern(String rawPattern) {
        Matcher m = PLACEHOLDER.matcher(rawPattern);
        if (!m.find()) return rawPattern;

        StringBuilder sb = new StringBuilder();
        m.reset();
        while (m.find()) {
            String key = m.group(1);
            Set<String> values = translations.get(key);
            if (values == null || values.isEmpty()) {
                Sync.LOGGER.warn("[Sync] Translation key '{}' used in filter but not found "
                        + "in any language file", key);
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
                continue;
            }

            // Sort longest-first so a longer translation is tried before a
            // shorter prefix that might otherwise match prematurely.
            String alternatives = values.stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|"));

            // (?i:...) makes just this group case-insensitive so player-typed
            // lowercase matches Title Case translation values.
            m.appendReplacement(sb, Matcher.quoteReplacement("(?i:" + alternatives + ")"));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
