package dev.overgrown.sync.networking;

import dev.overgrown.sync.Sync;
import net.minecraft.util.Identifier;

public class ModPackets {
    public static final Identifier KEY_PRESS_UPDATE         = Sync.identifier("key_press_update");
    public static final Identifier PLAYER_MODEL_TYPE_UPDATE = Sync.identifier("player_model_type_update");

    /** S->C  payload: int entityNetId, boolean hasDisguise, [DisguiseData if true] */
    public static final Identifier DISGUISE_UPDATE = Sync.identifier("disguise_update");
}