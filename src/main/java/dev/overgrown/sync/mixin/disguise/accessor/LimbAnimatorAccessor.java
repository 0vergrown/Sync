package dev.overgrown.sync.mixin.disguise.accessor;

import net.minecraft.entity.LimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LimbAnimator.class)
public interface LimbAnimatorAccessor {
    @Accessor("pos")
    float sync$getPos();

    @Accessor("pos")
    void sync$setPos(float pos);

    @Accessor("prevSpeed")
    float sync$getPrevSpeed();

    @Accessor("prevSpeed")
    void sync$setPrevSpeed(float prevSpeed);
}
