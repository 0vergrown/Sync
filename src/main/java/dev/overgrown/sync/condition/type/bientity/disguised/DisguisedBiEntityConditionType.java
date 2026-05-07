package dev.overgrown.sync.condition.type.bientity.disguised;

import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.data.disguise.client.ClientDisguiseManager;
import dev.overgrown.sync.registry.SyncBiEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class DisguisedBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<DisguisedBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData(),
        data -> new DisguisedBiEntityConditionType(),
        (conditionType, serializableData) -> serializableData.instance()
    );

    @Override
    public boolean test(BiEntityConditionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (actor == null || target == null) return false;

        if (!actor.getWorld().isClient()) {
            return DisguiseManager.isDisguisedAs(actor.getUuid(), target);
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return ClientDisguiseManager.isDisguisedAs(actor.getId(), target);
        }

        return false;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncBiEntityConditionTypes.DISGUISED;
    }
}
