package dev.overgrown.sync.networking;

import dev.overgrown.sync.Sync;
import net.minecraft.util.Identifier;

public class ModPackets {
    public static final Identifier KEY_PRESS_UPDATE = Sync.identifier("key_press_update");
    public static final Identifier PLAYER_MODEL_TYPE_UPDATE = Sync.identifier("player_model_type_update");

    /** Server to Client: int entityNetId, boolean hasDisguise, [DisguiseData if true] */
    public static final Identifier DISGUISE_UPDATE = Sync.identifier("disguise_update");

    /**
     * Server to Client: sends the full list of data-driven keybind definitions to the client
     * immediately after login so the client can register them in its controls screen.
     */
    public static final Identifier KEYBIND_SYNC = Sync.identifier("keybind_sync");

    /**
     * Client to Server: notifies the server of the local player's current camera perspective.
     */
    public static final Identifier PERSPECTIVE_UPDATE = Sync.identifier("perspective_update");
}