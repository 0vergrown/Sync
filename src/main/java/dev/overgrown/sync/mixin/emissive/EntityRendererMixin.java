package dev.overgrown.sync.mixin.emissive;

import dev.overgrown.sync.power.type.emissive.EmissivePowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EntityRenderer.class, priority = 998)
public abstract class EntityRendererMixin {

    @Inject(method = "getBlockLight", at = @At("HEAD"), cancellable = true)
    private void sync$makeEmissive(Entity entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof LivingEntity living) {
            List<EmissivePowerType> emissives = PowerHolderComponent.getPowerTypes(living, EmissivePowerType.class);
            if (emissives.isEmpty()) return;

            int max = emissives.get(0).light;
            for (EmissivePowerType p : emissives) {
                if (p.light > max) max = p.light;
            }
            cir.setReturnValue(max);
        }
    }
}
