package dev.overgrown.sync.factory.condition.bientity.colliding;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CollidingCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor  = pair.getLeft();
        Entity target = pair.getRight();

        Box actorBox = actor.getBoundingBox();

        // Shift the actor's AABB by the optional offset before testing intersection.
        if (data.isPresent("offset")) {
            Vec3d offset = data.get("offset");
            actorBox = actorBox.offset(offset);
        }

        return actorBox.intersects(target.getBoundingBox());
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("colliding"),
                new SerializableData()
                        // Vec3d default null and treated as optional via isPresent()
                        .add("offset", SerializableDataTypes.VECTOR, null),
                CollidingCondition::condition
        );
    }
}