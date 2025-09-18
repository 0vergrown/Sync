package dev.overgrown.sync;

import dev.overgrown.sync.utils.KeyPressManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class SyncServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register(KeyPressManager::serverTick);
    }
}