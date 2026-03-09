package dev.overgrown.sync.factory.action.bientity.transfer;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.toggle_transfer_mode.utils.TransferModeManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class TransferAction {

    static final Identifier DEFAULT_TRANSFER_SOURCE = Sync.identifier("transferred");

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor  = pair.getLeft();
        Entity target = pair.getRight();

        PowerHolderComponent actorComp  = PowerHolderComponent.KEY.maybeGet(actor).orElse(null);
        PowerHolderComponent targetComp = PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (actorComp == null || targetComp == null) return;

        String modeField = data.getString("mode");
        boolean stealing;
        if ("auto".equalsIgnoreCase(modeField)) {
            stealing = TransferModeManager.isStealing(actor);
        } else {
            stealing = !"give".equalsIgnoreCase(modeField);
        }

        PowerHolderComponent donorComp     = stealing ? targetComp : actorComp;
        PowerHolderComponent recipientComp = stealing ? actorComp  : targetComp;
        Entity donor     = stealing ? target : actor;
        Entity recipient = stealing ? actor  : target;

        Identifier transferSource  = data.get("transfer_source");
        boolean    stripFromDonor  = data.getBoolean("strip_from_donor");
        boolean    useSelectedSlot = data.getBoolean("use_selected_slot");

        final Identifier filterSource = (data.isPresent("source") && data.get("source") != null)
                ? (Identifier) data.get("source") : null;
        final PowerType<?> onlyPower  = data.isPresent("power") ? data.get("power") : null;

        final Identifier selectedSourceToGive;
        if (!stealing && useSelectedSlot && onlyPower == null && filterSource == null) {
            selectedSourceToGive = StolenPowerSlotManager.getSelectedSource(donor, transferSource);
            if (selectedSourceToGive == null) {
                Sync.LOGGER.debug("[Sync/Transfer] give/use_selected_slot: {} has no selected package.",
                        donor.getEntityName());
                return;
            }
        } else {
            selectedSourceToGive = null;
        }
        List<Pair<PowerType<?>, Identifier>> transfers = new ArrayList<>();

        if (onlyPower != null) {
            Map<PowerType<?>, List<Identifier>> srcMap = getPowerSourcesMap(donorComp);
            List<Identifier> knownSources = srcMap.getOrDefault(onlyPower, Collections.emptyList());
            for (Identifier src : knownSources) {
                if (filterSource == null || filterSource.equals(src)) {
                    transfers.add(new Pair<>(onlyPower, src));
                    break;
                }
            }

        } else if (filterSource != null) {
            for (PowerType<?> pt : donorComp.getPowersFromSource(filterSource)) {
                transfers.add(new Pair<>(pt, filterSource));
            }

        } else if (selectedSourceToGive != null) {
            List<PowerType<?>> packagePowers =
                    StolenPowerSlotManager.getPowersForSource(donor, selectedSourceToGive);
            if (packagePowers.isEmpty()) {
                Sync.LOGGER.debug("[Sync/Transfer] Package '{}' is empty for {}.",
                        selectedSourceToGive, donor.getEntityName());
                return;
            }
            for (PowerType<?> pt : packagePowers) {
                transfers.add(new Pair<>(pt, transferSource));
            }

        } else if (stealing) {
            Map<PowerType<?>, List<Identifier>> srcMap = getPowerSourcesMap(donorComp);
            for (Map.Entry<PowerType<?>, List<Identifier>> entry : new HashMap<>(srcMap).entrySet()) {
                for (Identifier src : new ArrayList<>(entry.getValue())) {
                    transfers.add(new Pair<>(entry.getKey(), src));
                }
            }

        } else {
            for (PowerType<?> pt : donorComp.getPowersFromSource(transferSource)) {
                transfers.add(new Pair<>(pt, transferSource));
            }
        }

        if (transfers.isEmpty()) {
            Sync.LOGGER.debug("[Sync/Transfer] No powers matched for {} → {} (mode={}).",
                    donor.getEntityName(), recipient.getEntityName(), stealing ? "steal" : "give");
            return;
        }

        boolean donorChanged = false;

        if (stealing) {
            Map<Identifier, List<PowerType<?>>> bySource = new LinkedHashMap<>();
            for (Pair<PowerType<?>, Identifier> t : transfers) {
                bySource.computeIfAbsent(t.getRight(), k -> new ArrayList<>()).add(t.getLeft());
            }

            for (Map.Entry<Identifier, List<PowerType<?>>> entry : bySource.entrySet()) {
                Identifier         src    = entry.getKey();
                List<PowerType<?>> powers = entry.getValue();

                for (PowerType<?> pt : powers) {
                    if (stripFromDonor) {
                        donorComp.removePower(pt, src);
                        donorChanged = true;
                    }
                    recipientComp.addPower(pt, transferSource);
                }
                StolenPowerSlotManager.registerSteal(
                        recipient.getUuid(), src, transferSource, powers);
            }

        } else {
            for (Pair<PowerType<?>, Identifier> t : transfers) {
                if (stripFromDonor) {
                    donorComp.removePower(t.getLeft(), t.getRight());
                    donorChanged = true;
                }
                recipientComp.addPower(t.getLeft(), transferSource);
            }

            if (stripFromDonor && selectedSourceToGive != null) {
                StolenPowerSlotManager.deregisterSource(donor.getUuid(), selectedSourceToGive);
            }
        }

        if (donorChanged) donorComp.sync();
        recipientComp.sync();

        Sync.LOGGER.debug("[Sync/Transfer] Transferred {} power(s) ({}) from {} to {} (→ source: {}).",
                transfers.size(), stealing ? "steal" : "give",
                donor.getEntityName(), recipient.getEntityName(), transferSource);

        Consumer<Entity> actorAction  = data.get("actor_action");
        Consumer<Entity> targetAction = data.get("target_action");
        if (actorAction  != null) actorAction.accept(actor);
        if (targetAction != null) targetAction.accept(target);
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("transfer"),
                new SerializableData()
                        .add("mode",              SerializableDataTypes.STRING,     "steal")
                        .add("source",            SerializableDataTypes.IDENTIFIER, null)
                        .add("power",             ApoliDataTypes.POWER_TYPE,        null)
                        .add("transfer_source",   SerializableDataTypes.IDENTIFIER, DEFAULT_TRANSFER_SOURCE)
                        .add("strip_from_donor",  SerializableDataTypes.BOOLEAN,    true)
                        .add("use_selected_slot", SerializableDataTypes.BOOLEAN,    true)
                        .add("actor_action",      ApoliDataTypes.ENTITY_ACTION,     null)
                        .add("target_action",     ApoliDataTypes.ENTITY_ACTION,     null),
                TransferAction::action
        );
    }

    @SuppressWarnings("unchecked")
    static Map<PowerType<?>, List<Identifier>> getPowerSourcesMap(PowerHolderComponent component) {
        Class<?> cls = component.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Field field = cls.getDeclaredField("powerSources");
                field.setAccessible(true);
                Object value = field.get(component);
                if (value instanceof Map) {
                    return (Map<PowerType<?>, List<Identifier>>) value;
                }
            } catch (NoSuchFieldException ignored) {
                cls = cls.getSuperclass();
            } catch (IllegalAccessException e) {
                Sync.LOGGER.warn("[Sync/Transfer] Cannot read powerSources on {}: {}",
                        cls.getName(), e.getMessage());
                return Collections.emptyMap();
            }
        }
        Sync.LOGGER.warn("[Sync/Transfer] powerSources field not found on {}",
                component.getClass().getName());
        return Collections.emptyMap();
    }
}