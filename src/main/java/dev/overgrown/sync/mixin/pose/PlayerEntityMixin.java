package dev.overgrown.sync.mixin.pose;

import dev.overgrown.sync.factory.power.type.pose.PosePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Inject(
			method = "updatePose",
			at = @At(
					"HEAD"
			),
			cancellable = true
	)
	private void forcePlayerPose(CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		PowerHolderComponent.getPowers(player, PosePower.class).stream()
				.max(Comparator.comparing(PosePower::getPriority))
				.ifPresent(power -> {
					player.setPose(power.getPose());
					ci.cancel();
				});
	}
}