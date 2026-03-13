package dev.overgrown.sync.mixin.modify_entity_behavior;

import dev.overgrown.sync.factory.power.type.modify_entity_behavior.ModifyEntityBehaviorPower;
import dev.overgrown.sync.factory.power.type.modify_entity_behavior.utils.BehaviorHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.VillagerHostilesSensor;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin {

    // This mixin used to have injects for "isHostile" and "isCloseEnoughForDanger"
    // Since I can't easily get a villager instance in here, I had to inject to "matches" instead of "isHostile"
    // That also made "isCloseEnoughForDanger" redundant, so I removed it

    @Inject(
            method = "matches",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void sync$markPlayerAsHostile(LivingEntity villager, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (!(target instanceof PlayerEntity player) || player.isCreative()) return;

        BehaviorHelper behaviorHelper = new BehaviorHelper(player, villager);

        if (behaviorHelper.checkEntity()) {
            if (behaviorHelper.behaviorMatches(ModifyEntityBehaviorPower.EntityBehavior.HOSTILE)) {
                final float distanceRequired = 8.0f;
                if (player.squaredDistanceTo(villager) <= (double) (distanceRequired * distanceRequired)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}