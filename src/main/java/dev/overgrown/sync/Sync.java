package dev.overgrown.sync;

import dev.overgrown.sync.factory.power.type.ActionOnDeathPower;
import dev.overgrown.sync.factory.registry.SyncTypeRegistry;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.utils.KeyPressManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
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

        HAS_ASPECTSLIB = FabricLoader.getInstance().isModLoaded("aspectslib");
        if (HAS_ASPECTSLIB) {
            LOGGER.info("AspectsLib detected - compatibility enabled");
        }

        NamespaceAlias.addAlias("apoli", MOD_ID);
    }
}