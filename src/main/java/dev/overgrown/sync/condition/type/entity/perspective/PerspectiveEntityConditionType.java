package dev.overgrown.sync.condition.type.entity.perspective;

import dev.overgrown.sync.condition.type.entity.perspective.util.PerspectiveManager;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PerspectiveEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PerspectiveEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("perspectives", SerializableDataTypes.STRINGS),
        data -> new PerspectiveEntityConditionType(data.get("perspectives")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("perspectives", conditionType.perspectives)
    );

    private final List<String> perspectives;

    public PerspectiveEntityConditionType(List<String> perspectives) {
        this.perspectives = perspectives;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        if (!(context.entity() instanceof PlayerEntity player)) {
            return false;
        }
        if (player.getWorld().isClient()) {
            return false;
        }
        String current = PerspectiveManager.getPerspective(player);
        return perspectives.contains(current);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.PERSPECTIVE;
    }
}
