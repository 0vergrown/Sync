package dev.overgrown.sync.mixin.prevent_taming;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.overgrown.sync.factory.power.type.prevent_taming.utils.TameUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(OcelotEntity.class)
public abstract class OcelotEntityMixin extends AnimalEntity {

    protected OcelotEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
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