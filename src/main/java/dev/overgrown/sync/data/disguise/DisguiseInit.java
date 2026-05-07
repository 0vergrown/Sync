package dev.overgrown.sync.data.disguise;

import dev.overgrown.sync.data.disguise.payload.s2c.DisguiseUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class DisguiseInit {

    private DisguiseInit() {}

    public static void init() {
        PayloadTypeRegistry.playS2C().register(DisguiseUpdatePayload.ID, DisguiseUpdatePayload.CODEC);
    }
}
