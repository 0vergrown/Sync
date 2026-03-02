package dev.overgrown.sync.factory.action.entity.disguise_as;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DisguiseAsAction {

    private static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            Sync.LOGGER.warn("sync:disguise_as – entity '{}' is not a LivingEntity, skipping.",
                    entity.getType().getUntranslatedName());
            return;
        }

        if (entity.getWorld().isClient()) return;

        boolean overwrite = data.getBoolean("overwrite");
        if (!overwrite && DisguiseManager.isDisguised(entity.getUuid())) return;

        if (data.isPresent("before_action")) {
            ((ActionFactory<Entity>.Instance) data.get("before_action")).accept(entity);
        }

        Identifier typeId = data.getId("entity_type");

        if (!Registries.ENTITY_TYPE.containsId(typeId)) {
            Sync.LOGGER.warn("sync:disguise_as – unknown entity type '{}', skipping.", typeId);
            return;
        }

        EntityType<?> entityType = Registries.ENTITY_TYPE.get(typeId);

        Text displayName = data.isPresent("display_name")
                ? data.get("display_name")
                : Text.translatable(entityType.getTranslationKey());

        DisguiseManager.forceApplyDisguise(living, new DisguiseData(typeId, -1, null, displayName));

        if (data.isPresent("after_action")) {
            ((ActionFactory<Entity>.Instance) data.get("after_action")).accept(entity);
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("disguise_as"),
                new SerializableData()
                        .add("entity_type", SerializableDataTypes.IDENTIFIER)
                        .add("display_name", SerializableDataTypes.TEXT, null)
                        .add("overwrite", SerializableDataTypes.BOOLEAN, true)
                        .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("after_action", ApoliDataTypes.ENTITY_ACTION, null),
                DisguiseAsAction::action
        );
    }
}