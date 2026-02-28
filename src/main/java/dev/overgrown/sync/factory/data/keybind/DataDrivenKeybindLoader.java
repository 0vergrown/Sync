package dev.overgrown.sync.factory.data.keybind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.overgrown.sync.Sync;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Server-side resource-reload listener that reads keybind definitions from
 * {@code data/<namespace>/keybinds/<id>.json}.
 *
 * <p>Loaded definitions are held in {@link #LOADED} and sent to each client
 * the moment they join the server (see {@code Sync#syncKeybindsToPlayer}).
 *
 * <h3>JSON format</h3>
 * <pre>{@code
 * {
 *   "key":      "key.keyboard.h",          // required – GLFW translation key
 *   "category": "key.category.my_mod",     // required – controls-screen category
 *   "name":     "My Cool Ability"          // optional – human-readable label hint
 * }
 * }</pre>
 */
public class DataDrivenKeybindLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** All successfully parsed keybind definitions after the last data-pack reload. */
    public static List<DataDrivenKeybindDefinition> LOADED = new ArrayList<>();

    public DataDrivenKeybindLoader() {
        super(GSON, "keybinds");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        List<DataDrivenKeybindDefinition> results = new ArrayList<>();

        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            Identifier id  = entry.getKey();
            JsonElement el = entry.getValue();

            if (!el.isJsonObject()) {
                Sync.LOGGER.warn("[Sync/Keybinds] Skipping '{}': not a JSON object.", id);
                continue;
            }

            JsonObject obj = el.getAsJsonObject();

            if (!obj.has("key") || !obj.has("category")) {
                Sync.LOGGER.warn("[Sync/Keybinds] Skipping '{}': missing required field(s) 'key' / 'category'.", id);
                continue;
            }

            String key      = obj.get("key").getAsString();
            String category = obj.get("category").getAsString();
            String name     = obj.has("name") ? obj.get("name").getAsString() : null;

            results.add(new DataDrivenKeybindDefinition(id, key, category, name));
        }

        LOADED = results;
        Sync.LOGGER.info("[Sync/Keybinds] Loaded {} data-driven keybind(s).", LOADED.size());
    }

    @Override
    public Identifier getFabricId() {
        return Sync.identifier("keybinds");
    }
}