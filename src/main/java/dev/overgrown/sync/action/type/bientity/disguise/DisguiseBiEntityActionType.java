package dev.overgrown.sync.action.type.bientity.disguise;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class DisguiseBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<DisguiseBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("overwrite", SerializableDataTypes.BOOLEAN, true),
        data -> new DisguiseBiEntityActionType(data.get("overwrite")),
        (actionType, serializableData) -> serializableData.instance()
            .set("overwrite", actionType.overwrite)
    );

    private final boolean overwrite;

    public DisguiseBiEntityActionType(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (actor == null || target == null) return;

        if (!(actor instanceof LivingEntity livingActor)) {
            Sync.LOGGER.warn("sync:disguise – actor '{}' is not a LivingEntity, skipping.",
                actor.getType().getUntranslatedName());
            return;
        }
        DisguiseManager.applyDisguise(livingActor, target, overwrite);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.DISGUISE;
    }
}
