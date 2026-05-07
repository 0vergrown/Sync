package dev.overgrown.sync.condition.type.entity.velocity;

import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class VelocityEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<VelocityEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("x", SerializableDataTypes.FLOAT.optional(), Optional.empty())
            .add("y", SerializableDataTypes.FLOAT.optional(), Optional.empty())
            .add("z", SerializableDataTypes.FLOAT.optional(), Optional.empty())
            .add("axes", SerializableDataTypes.STRINGS.optional(), Optional.empty())
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.FLOAT, 0f),
        data -> new VelocityEntityConditionType(
            data.get("x"),
            data.get("y"),
            data.get("z"),
            data.get("axes"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("x", conditionType.x)
            .set("y", conditionType.y)
            .set("z", conditionType.z)
            .set("axes", conditionType.axes)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<Float> x;
    private final Optional<Float> y;
    private final Optional<Float> z;
    private final Optional<List<String>> axes;
    private final Comparison comparison;
    private final float compareTo;

    public VelocityEntityConditionType(Optional<Float> x, Optional<Float> y, Optional<Float> z,
                                       Optional<List<String>> axes, Comparison comparison, float compareTo) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.axes = axes;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        Vec3d velocity = context.entity().getVelocity();

        if (x.isPresent() || y.isPresent() || z.isPresent()) {
            if (x.isPresent() && !comparison.compare((float) velocity.x, x.get())) return false;
            if (y.isPresent() && !comparison.compare((float) velocity.y, y.get())) return false;
            if (z.isPresent() && !comparison.compare((float) velocity.z, z.get())) return false;
            return true;
        }

        if (axes.isPresent()) {
            double combined = 0.0;
            for (String axis : axes.get()) {
                combined += switch (axis.toLowerCase()) {
                    case "x" -> velocity.x;
                    case "y" -> velocity.y;
                    case "z" -> velocity.z;
                    default -> 0.0;
                };
            }
            return comparison.compare((float) combined, compareTo);
        }

        return true;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.VELOCITY;
    }
}
