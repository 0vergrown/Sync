package dev.overgrown.sync.action.type.entity.radial_menu.client;

import dev.overgrown.sync.action.type.entity.radial_menu.payload.s2c.OpenRadialMenuPayload;
import dev.overgrown.sync.action.type.entity.radial_menu.utils.RadialMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class RadialMenuClient {

    private RadialMenuClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OpenRadialMenuPayload.ID, (payload, context) ->
            context.client().execute(() -> {
                RadialMenu menu = new RadialMenu(payload.entries(), payload.menuTexture());
                context.client().setScreen(new RadialMenuScreen(menu));
            })
        );
    }
}
