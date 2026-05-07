package dev.overgrown.sync.power.type.action_on_sending_message.util;

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

public final class TranslationKeyResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("#\\{([^}]+)}");

    private static volatile Map<String, Set<String>> translations = Collections.emptyMap();

    private TranslationKeyResolver() {}

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

                        try (DirectoryStream<Path> langFiles = Files.newDirectoryStream(langDir, "*.json")) {
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
        Sync.LOGGER.info("[Sync] Loaded {} translation key(s) from {} language resource(s) via ResourceManager",
            map.size(), fileCount);
    }

    public static String expandPattern(String rawPattern) {
        Matcher m = PLACEHOLDER.matcher(rawPattern);
        if (!m.find()) return rawPattern;

        StringBuilder sb = new StringBuilder();
        m.reset();
        while (m.find()) {
            String key = m.group(1);
            Set<String> values = translations.get(key);
            if (values == null || values.isEmpty()) {
                Sync.LOGGER.warn("[Sync] Translation key '{}' used in filter but not found in any language file", key);
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
                continue;
            }

            String alternatives = values.stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

            m.appendReplacement(sb, Matcher.quoteReplacement("(?i:" + alternatives + ")"));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
