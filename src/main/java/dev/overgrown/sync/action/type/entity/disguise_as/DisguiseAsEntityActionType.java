package dev.overgrown.sync.action.type.entity.disguise_as;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.disguise.DisguiseData;
import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DisguiseAsEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<DisguiseAsEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_type", SerializableDataTypes.IDENTIFIER)
            .add("nbt", SerializableDataTypes.NBT_COMPOUND.optional(), Optional.empty())
            .add("overwrite", SerializableDataTypes.BOOLEAN, true)
            .add("before_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("after_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new DisguiseAsEntityActionType(
            data.get("entity_type"),
            data.get("nbt"),
            data.get("overwrite"),
            data.get("before_action"),
            data.get("after_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("entity_type", actionType.entityType)
            .set("nbt", actionType.nbt)
            .set("overwrite", actionType.overwrite)
            .set("before_action", actionType.beforeAction)
            .set("after_action", actionType.afterAction)
    );

    private final Identifier entityType;
    private final Optional<NbtCompound> nbt;
    private final boolean overwrite;
    private final Optional<EntityAction> beforeAction;
    private final Optional<EntityAction> afterAction;

    public DisguiseAsEntityActionType(Identifier entityType, Optional<NbtCompound> nbt, boolean overwrite,
                                      Optional<EntityAction> beforeAction, Optional<EntityAction> afterAction) {
        this.entityType = entityType;
        this.nbt = nbt;
        this.overwrite = overwrite;
        this.beforeAction = beforeAction;
        this.afterAction = afterAction;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (!(context.entity() instanceof LivingEntity living)) {
            Sync.LOGGER.warn("sync:disguise_as – entity '{}' is not a LivingEntity, skipping.",
                context.entity().getType().getUntranslatedName());
            return;
        }
        if (living.getWorld().isClient()) return;
        if (!overwrite && DisguiseManager.isDisguised(living.getUuid())) return;

        if (!Registries.ENTITY_TYPE.containsId(entityType)) {
            Sync.LOGGER.warn("sync:disguise_as – unknown entity type '{}', skipping.", entityType);
            return;
        }

        beforeAction.ifPresent(action -> action.execute(living));

        DisguiseManager.forceApplyDisguise(living, new DisguiseData(entityType, -1, null, nbt.orElse(null)));

        afterAction.ifPresent(action -> action.execute(living));
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.DISGUISE_AS;
    }
}
