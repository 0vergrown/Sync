package dev.overgrown.sync.factory.action.bientity.transfer;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.toggle_transfer_mode.utils.TransferModeManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Consumer;

public class TransferAction {

    static final Identifier DEFAULT_TRANSFER_SOURCE = Sync.identifier("transferred");

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor = pair.getLeft();
        Entity target = pair.getRight();

        PowerHolderComponent actorComp = PowerHolderComponent.KEY.maybeGet(actor).orElse(null);
        PowerHolderComponent targetComp = PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (actorComp == null || targetComp == null) return;

        String modeField = data.getString("mode");
        boolean stealing;
        if ("auto".equalsIgnoreCase(modeField)) {
            stealing = TransferModeManager.isStealing(actor);
        } else {
            stealing = !"give".equalsIgnoreCase(modeField);
        }

        Identifier transferSource = data.get("transfer_source");
        boolean stripFromDonor = data.getBoolean("strip_from_donor");

        if (stealing) {
            doSteal(data, actor, target, actorComp, targetComp, transferSource, stripFromDonor);
        } else {
            doGive(data, actor, target, actorComp, targetComp, transferSource, stripFromDonor);
        }

        Consumer<Entity> actorAction = data.get("actor_action");
        Consumer<Entity> targetAction = data.get("target_action");
        if (actorAction != null) actorAction.accept(actor);
        if (targetAction != null) targetAction.accept(target);
    }

    private static void doSteal(SerializableData.Instance data,
                                Entity actor, Entity target,
                                PowerHolderComponent actorComp, PowerHolderComponent targetComp,
                                Identifier transferSource, boolean stripFromDonor) {

        Identifier filterSource = data.isPresent("source") ? data.get("source") : null;

        Map<Identifier, List<PowerType<?>>> toSteal = new LinkedHashMap<>();

        if (filterSource != null) {
            List<PowerType<?>> powers = targetComp.getPowersFromSource(filterSource);
            if (!powers.isEmpty()) {
                toSteal.put(filterSource, StolenPowerSlotManager.filterTopLevel(powers));
            }
        } else {
            Set<Identifier> seenSources = new LinkedHashSet<>();
            for (Power p : new ArrayList<>(targetComp.getPowers())) {
                for (Identifier src : targetComp.getSources(p.getType())) {
                    if (!src.equals(transferSource)) {
                        seenSources.add(src);
                    }
                }
            }
            for (Identifier src : seenSources) {
                List<PowerType<?>> powers = targetComp.getPowersFromSource(src);
                List<PowerType<?>> topLevel = StolenPowerSlotManager.filterTopLevel(powers);
                if (!topLevel.isEmpty()) {
                    toSteal.put(src, topLevel);
                }
            }
        }

        if (toSteal.isEmpty()) return;

        boolean donorChanged = false;
        for (Map.Entry<Identifier, List<PowerType<?>>> entry : toSteal.entrySet()) {
            Identifier originalSource = entry.getKey();
            List<PowerType<?>> powers = entry.getValue();

            for (PowerType<?> pt : powers) {
                if (stripFromDonor) {
                    targetComp.removePower(pt, originalSource);
                    donorChanged = true;
                }
                actorComp.addPower(pt, transferSource);
            }

            StolenPowerSlotManager.registerSteal(actor.getUuid(), originalSource, powers);
        }

        if (donorChanged) targetComp.sync();
        actorComp.sync();
    }

    private static void doGive(SerializableData.Instance data,
                               Entity actor, Entity target,
                               PowerHolderComponent actorComp, PowerHolderComponent targetComp,
                               Identifier transferSource, boolean stripFromDonor) {

        Identifier selectedOriginal = StolenPowerSlotManager.getSelectedSource(actor);
        if (selectedOriginal == null) return;

        List<PowerType<?>> packagePowers =
                StolenPowerSlotManager.getPowersForSource(actor, selectedOriginal);
        if (packagePowers.isEmpty()) return;

        boolean donorChanged = false;
        for (PowerType<?> pt : packagePowers) {
            if (stripFromDonor) {
                actorComp.removePower(pt, transferSource);
                donorChanged = true;
            }
            targetComp.addPower(pt, selectedOriginal);
        }

        if (stripFromDonor) {
            StolenPowerSlotManager.deregisterSource(actor.getUuid(), selectedOriginal);
        }

        if (donorChanged) actorComp.sync();
        targetComp.sync();
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("transfer"),
                new SerializableData()
                        .add("mode", SerializableDataTypes.STRING, "steal")
                        .add("source", SerializableDataTypes.IDENTIFIER, null)
                        .add("transfer_source", SerializableDataTypes.IDENTIFIER, DEFAULT_TRANSFER_SOURCE)
                        .add("strip_from_donor", SerializableDataTypes.BOOLEAN, true)
                        .add("actor_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("target_action", ApoliDataTypes.ENTITY_ACTION, null),
                TransferAction::action
        );
    }
}
