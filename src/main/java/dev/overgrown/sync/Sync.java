package dev.overgrown.sync;

import dev.overgrown.sync.factory.registry.SyncTypeRegistry;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sync implements ModInitializer {
    public static final String MOD_ID = "sync";
    public static String VERSION = "";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean HAS_ASPECTSLIB = false; // Flag to check if AspectsLib is present

    public static Identifier identifier(String path) {
        return new Identifier(Sync.MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        SyncTypeRegistry.register();

        NamespaceAlias.addAlias("apoli", MOD_ID);
    }
}