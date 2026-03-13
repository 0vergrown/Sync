package dev.overgrown.sync.mixin.prevent_taming;

import dev.overgrown.sync.factory.power.type.prevent_taming.utils.TameUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin extends AnimalEntity implements Tameable {

    protected TameableEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "setOwner",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$preventTaming(PlayerEntity player, CallbackInfo ci) {
        // Works as a fail-safe and also to prevent taming through the "Tame" Bi-entity action
        TameUtil.preventTameAction(player, this, ci);
    }
}