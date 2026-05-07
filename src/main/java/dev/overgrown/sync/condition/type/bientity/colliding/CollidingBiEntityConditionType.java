package dev.overgrown.sync.condition.type.bientity.colliding;

import dev.overgrown.sync.registry.SyncBiEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CollidingBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<CollidingBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("offset", SerializableDataTypes.VECTOR.optional(), Optional.empty()),
        data -> new CollidingBiEntityConditionType(data.get("offset")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("offset", conditionType.offset)
    );

    private final Optional<Vec3d> offset;

    public CollidingBiEntityConditionType(Optional<Vec3d> offset) {
        this.offset = offset;
    }

    @Override
    public boolean test(BiEntityConditionContext context) {
        Box actorBox = context.actor().getBoundingBox();
        if (offset.isPresent()) {
            actorBox = actorBox.offset(offset.get());
        }
        return actorBox.intersects(context.target().getBoundingBox());
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncBiEntityConditionTypes.COLLIDING;
    }
}
