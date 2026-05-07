package dev.overgrown.sync.mixin.prevent_teleport;

import dev.overgrown.sync.power.type.prevent_teleport.PreventTeleportPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class PreventTeleportMixin {

    @Inject(
        method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
        at = @At("HEAD"),
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
        List<PreventTeleportPowerType> powers = PowerHolderComponent.getPowerTypes(self, PreventTeleportPowerType.class);
        if (!powers.isEmpty()) {
            powers.forEach(PreventTeleportPowerType::onTeleportPrevented);
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void sync$preventDimensionTeleport(
        ServerWorld targetWorld,
        double x, double y, double z,
        float yaw, float pitch,
        CallbackInfo ci
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        List<PreventTeleportPowerType> powers = PowerHolderComponent.getPowerTypes(self, PreventTeleportPowerType.class);
        if (!powers.isEmpty()) {
            powers.forEach(PreventTeleportPowerType::onTeleportPrevented);
            ci.cancel();
        }
    }

    @Inject(
        method = "teleportTo",
        at = @At("HEAD"),
        cancellable = true
    )
    private void sync$preventTeleportTo(
        TeleportTarget teleportTarget,
        CallbackInfoReturnable<Entity> cir
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        List<PreventTeleportPowerType> powers = PowerHolderComponent.getPowerTypes(self, PreventTeleportPowerType.class);
        if (!powers.isEmpty()) {
            powers.forEach(PreventTeleportPowerType::onTeleportPrevented);
            cir.setReturnValue(self);
        }
    }

    @Inject(
        method = "requestTeleport",
        at = @At("HEAD"),
        cancellable = true
    )
    private void sync$preventRequestTeleport(
        double destX, double destY, double destZ,
        CallbackInfo ci
    ) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (!PowerHolderComponent.getPowerTypes(self, PreventTeleportPowerType.class).isEmpty()) {
            ci.cancel();
        }
    }
}
