package dev.overgrown.sync.networking;

import dev.overgrown.sync.Sync;
import net.minecraft.util.Identifier;

public class ModPackets {
    public static final Identifier KEY_PRESS_UPDATE         = Sync.identifier("key_press_update");
    public static final Identifier PLAYER_MODEL_TYPE_UPDATE = Sync.identifier("player_model_type_update");

    /** Server to Client: int entityNetId, boolean hasDisguise, [DisguiseData if true] */
    public static final Identifier DISGUISE_UPDATE = Sync.identifier("disguise_update");

    /**
     * S→C: sends the full list of data-driven keybind definitions to the client
     * immediately after login so the client can register them in its controls screen.
     *
     * <p>Packet layout:
     * <pre>
     *   int   count
     *   for each definition:
     *     Identifier  id          (namespace + path)
     *     String      key         e.g. "key.keyboard.h"
     *     String      category    e.g. "key.category.my_mod"
     *     boolean     hasName
     *     [String     name]       only present when hasName == true
     * </pre>
     */
    public static final Identifier KEYBIND_SYNC = Sync.identifier("keybind_sync");
}