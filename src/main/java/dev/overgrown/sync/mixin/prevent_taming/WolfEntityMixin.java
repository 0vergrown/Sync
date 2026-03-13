package dev.overgrown.sync.mixin.prevent_taming;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.prevent_taming.utils.TameUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity implements Angerable {

    protected WolfEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "interactMob",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/random/Random;nextInt(I)I"
            )
    )
    private int sync$preventTaming(int original, PlayerEntity player) {
        return TameUtil.preventTaming(original, player, this);
    }
}