package dev.overgrown.sync.mixin.prevent_sprinting_particles;

import dev.overgrown.sync.factory.power.type.prevent_sprinting_particles.PreventSprintingParticlesPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PreventSprintingParticlesMixin {

    @Inject(
            method = "spawnSprintingParticles",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventSprintingParticles(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        // Check if entity has the PreventSprintingParticlesPower
        PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
        for (PreventSprintingParticlesPower power : component.getPowers(PreventSprintingParticlesPower.class)) {
            if (power.isActive()) {
                ci.cancel();
                break;
            }
        }
    }
}