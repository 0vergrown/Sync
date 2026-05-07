package dev.overgrown.sync.mixin.prevent_sprinting_particles;

import dev.overgrown.sync.power.type.prevent_sprinting_particles.PreventSprintingParticlesPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public class PreventSprintingParticlesMixin {

    @Inject(method = "spawnSprintingParticles", at = @At("HEAD"), cancellable = true)
    private void sync$preventSprintingParticles(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        List<PreventSprintingParticlesPowerType> powers = PowerHolderComponent.getPowerTypes(entity, PreventSprintingParticlesPowerType.class);
        for (PreventSprintingParticlesPowerType power : powers) {
            if (power.isActive()) {
                ci.cancel();
                break;
            }
        }
    }
}
