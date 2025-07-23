package dev.overgrown.sync.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.HashMap;
import java.util.Map;

public class SyncDataTypes {
    @SuppressWarnings("unchecked")
    public static final SerializableDataType<Map<String, Integer>> STRING_INT_MAP =
            new SerializableDataType<>(
                    (Class<Map<String, Integer>>) (Class<?>) Map.class,
                    (buf, map) -> {
                        buf.writeInt(map.size());
                        for (Map.Entry<String, Integer> entry : map.entrySet()) {
                            buf.writeString(entry.getKey());
                            buf.writeInt(entry.getValue());
                        }
                    },
                    buf -> {
                        int size = buf.readInt();
                        Map<String, Integer> map = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            map.put(buf.readString(), buf.readInt());
                        }
                        return map;
                    },
                    json -> {
                        if (!json.isJsonObject()) return null;
                        JsonObject obj = json.getAsJsonObject();
                        Map<String, Integer> map = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                map.put(entry.getKey(), entry.getValue().getAsInt());
                            }
                        }
                        return map;
                    }
            );
}