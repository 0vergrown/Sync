package dev.overgrown.sync.mixin.prevent_teleport;

import dev.overgrown.sync.factory.power.type.prevent_teleport.PreventTeleportPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class PreventTeleportMixin {

    // -----------------------------------------------------------------------
    // 1. Primary teleport — /tp, TeleportCommand, spectator menus,
    //    setCameraEntity, and actions using this overload.
    //    When crossing dimensions this method delegates to overload #2 below;
    //    cancelling here prevents that call as well.
    // -----------------------------------------------------------------------
    @Inject(
            method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventTeleport(
            ServerWorld world,
            double destX, double destY, double destZ,
            Set<PositionFlag> flags,
            float yaw, float pitch,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        List<PreventTeleportPower> powers = PowerHolderComponent.getPowers(self, PreventTeleportPower.class);
        if (!powers.isEmpty()) {
            powers.forEach(PreventTeleportPower::onTeleportPrevented);
            cir.setReturnValue(false);
        }
    }

    // -----------------------------------------------------------------------
    // 2. Void dimension-teleport: Called directly by Sync's
    //    Teleport To Spawn and Teleport To Location when the target is a
    //    different dimension.
    // -----------------------------------------------------------------------
    @Inject(
            method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventDimensionTeleport(
            ServerWorld targetWorld,
            double x, double y, double z,
            float yaw, float pitch,
            CallbackInfo ci
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        List<PreventTeleportPower> powers = PowerHolderComponent.getPowers(self, PreventTeleportPower.class);
        if (!powers.isEmpty()) {
            powers.forEach(PreventTeleportPower::onTeleportPrevented);
            ci.cancel();
        }
    }

    // -----------------------------------------------------------------------
    // 3. Portal / end-gateway dimension transitions.
    //    Returning 'this' (still in the source world) signals to the caller
    //    that no transition occurred, keeping the player where they are.
    // -----------------------------------------------------------------------
    @Inject(
            method = "moveToWorld",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventMoveToWorld(
            ServerWorld destination,
            CallbackInfoReturnable<Entity> cir
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        List<PreventTeleportPower> powers = PowerHolderComponent.getPowers(self, PreventTeleportPower.class);
        if (!powers.isEmpty()) {
            powers.forEach(PreventTeleportPower::onTeleportPrevented);
            // Return the player entity itself - caller interprets this as
            // "entity is still in the original world, no transition happened".
            cir.setReturnValue(self);
        }
    }

    // -----------------------------------------------------------------------
    // 4. Entity#requestTeleport — used by Sync's Random Teleport action and
    //    any other code that calls entity.requestTeleport() on a player.
    //    Note: vanilla's wakeUp() calls networkHandler.requestTeleport()
    //    directly and bypasses this override, so sleep mechanics are safe.
    // -----------------------------------------------------------------------
    @Inject(
            method = "requestTeleport",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventRequestTeleport(
            double destX, double destY, double destZ,
            CallbackInfo ci
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (PowerHolderComponent.hasPower(self, PreventTeleportPower.class)) {
            // No entity_action here, random_teleport may attempt hundreds of
            // times per call; the action fires from the dedicated overloads above.
            ci.cancel();
        }
    }
}