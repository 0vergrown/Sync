package dev.overgrown.sync;

import dev.overgrown.sync.factory.power.type.ActionOnDeathPower;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.utils.KeyPressManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register(KeyPressManager::serverTick);

        // Register death event handler
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity) {
                PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                component.getPowers(ActionOnDeathPower.class).forEach(power -> {
                    power.onDeath(damageSource, entity.getMaxHealth()); // Use max health as damage amount
                });
            }
        });

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