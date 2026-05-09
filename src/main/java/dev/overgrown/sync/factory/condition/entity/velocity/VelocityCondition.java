package dev.overgrown.sync.factory.condition.entity.velocity;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class VelocityCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        Vec3d velocity = entity.getVelocity();

        // Per-axis exact checks (x / y / z fields):
        // If any explicit axis field is present we compare that component
        // directly and ALL present components must pass.
        boolean hasAxisField = data.isPresent("x") || data.isPresent("y") || data.isPresent("z");
        if (hasAxisField) {
            Comparison comparison = data.get("comparison");
            if (data.isPresent("x") && !comparison.compare((float) velocity.x, data.getFloat("x"))) return false;
            if (data.isPresent("y") && !comparison.compare((float) velocity.y, data.getFloat("y"))) return false;
            if (data.isPresent("z") && !comparison.compare((float) velocity.z, data.getFloat("z"))) return false;
            return true;
        }

        // Multi-axis speed check (axes + compare_to):
        // Sum the signed velocity of every listed axis and compare to compare_to.
        if (data.isPresent("axes")) {
            List<String> axes = data.get("axes");
            Comparison comparison = data.get("comparison");
            float compareTo = data.getFloat("compare_to");

            double combined = 0.0;
            for (String axis : axes) {
                combined += switch (axis.toLowerCase()) {
                    case "x" -> velocity.x;
                    case "y" -> velocity.y;
                    case "z" -> velocity.z;
                    default  -> 0.0;
                };
            }
            return comparison.compare((float) combined, compareTo);
        }

        // No fields specified (nothing to test, condition passes vacuously).
        return true;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("velocity"),
                new SerializableData()
                        // Per-component exact checks
                        .add("x", SerializableDataTypes.FLOAT, null)
                        .add("y", SerializableDataTypes.FLOAT, null)
                        .add("z", SerializableDataTypes.FLOAT, null)
                        // Multi-axis speed check
                        .add("axes", SerializableDataTypes.STRINGS, null)
                        .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                        .add("compare_to", SerializableDataTypes.FLOAT, 0f),
                VelocityCondition::condition
        );
    }
}