package dev.overgrown.sync;

import dev.overgrown.sync.factory.registry.SyncTypeRegistry;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.utils.KeyPressManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
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
		HAS_ASPECTSLIB = FabricLoader.getInstance().isModLoaded("aspectslib");

		NamespaceAlias.addAlias("apoli", MOD_ID);
		SyncTypeRegistry.register();

		if (HAS_ASPECTSLIB) {
			LOGGER.info("AspectsLib detected - compatibility enabled");
		}

		ServerPlayNetworking.registerGlobalReceiver(
				ModPackets.KEY_PRESS_UPDATE,
				(server, player, handler, buf, responseSender) -> {
					String key = buf.readString();
					boolean pressed = buf.readBoolean();
					server.execute(() ->
							KeyPressManager.updateKeyState(player.getUuid(), key, pressed)
					);
				}
		);

		ServerPlayConnectionEvents.DISCONNECT.register(
				(handler, server) ->
						KeyPressManager.removePlayer(handler.player.getUuid())
		);
	}
}