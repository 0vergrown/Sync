package dev.overgrown.sync.factory.condition.entity.attached_to_rope;

import dev.overgrown.sync.rope.common.RopeManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class AttachedToRopeCondition {

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                new Identifier("sync", "attached_to_rope"),
                new SerializableData(),
                (data, entity) -> {
                    if (!(entity instanceof PlayerEntity player)) return false;
                    return RopeManager.has(player.getUuid());
                }
        );
    }
}