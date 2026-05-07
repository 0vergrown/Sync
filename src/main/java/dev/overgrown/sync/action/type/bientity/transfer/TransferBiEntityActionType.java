package dev.overgrown.sync.action.type.bientity.transfer;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.transfer.StolenPowerSlotManager;
import dev.overgrown.sync.data.transfer.TransferModeManager;
import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TransferBiEntityActionType extends BiEntityActionType {

    private static final Identifier DEFAULT_TRANSFER_SOURCE = Sync.identifier("transferred");

    public static final TypedDataObjectFactory<TransferBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("mode", SerializableDataTypes.STRING, "steal")
            .add("source", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("transfer_source", SerializableDataTypes.IDENTIFIER, DEFAULT_TRANSFER_SOURCE)
            .add("strip_from_donor", SerializableDataTypes.BOOLEAN, true)
            .add("actor_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("target_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new TransferBiEntityActionType(
            data.get("mode"),
            data.get("source"),
            data.get("transfer_source"),
            data.get("strip_from_donor"),
            data.get("actor_action"),
            data.get("target_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("mode", actionType.mode)
            .set("source", actionType.source)
            .set("transfer_source", actionType.transferSource)
            .set("strip_from_donor", actionType.stripFromDonor)
            .set("actor_action", actionType.actorAction)
            .set("target_action", actionType.targetAction)
    );

    private final String mode;
    private final Optional<Identifier> source;
    private final Identifier transferSource;
    private final boolean stripFromDonor;
    private final Optional<EntityAction> actorAction;
    private final Optional<EntityAction> targetAction;

    public TransferBiEntityActionType(String mode, Optional<Identifier> source, Identifier transferSource,
                                      boolean stripFromDonor, Optional<EntityAction> actorAction,
                                      Optional<EntityAction> targetAction) {
        this.mode = mode;
        this.source = source;
        this.transferSource = transferSource;
        this.stripFromDonor = stripFromDonor;
        this.actorAction = actorAction;
        this.targetAction = targetAction;
    }

    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (actor == null || target == null) return;

        PowerHolderComponent actorComp = PowerHolderComponent.KEY.maybeGet(actor).orElse(null);
        PowerHolderComponent targetComp = PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (actorComp == null || targetComp == null) return;

        boolean stealing;
        if ("auto".equalsIgnoreCase(mode)) {
            stealing = TransferModeManager.isStealing(actor);
        } else {
            stealing = !"give".equalsIgnoreCase(mode);
        }

        if (stealing) {
            doSteal(actor, target, actorComp, targetComp);
        } else {
            doGive(actor, target, actorComp, targetComp);
        }

        actorAction.ifPresent(a -> a.execute(actor));
        targetAction.ifPresent(a -> a.execute(target));
    }

    private void doSteal(Entity actor, Entity target,
                         PowerHolderComponent actorComp, PowerHolderComponent targetComp) {
        Map<Identifier, List<Power>> toSteal = new LinkedHashMap<>();

        if (source.isPresent()) {
            List<Power> powers = targetComp.getPowersFromSource(source.get());
            if (!powers.isEmpty()) {
                toSteal.put(source.get(), StolenPowerSlotManager.filterTopLevel(powers));
            }
        } else {
            Set<Identifier> seenSources = new LinkedHashSet<>();
            for (Power p : new ArrayList<>(targetComp.getPowers(true))) {
                for (Identifier src : targetComp.getSources(p)) {
                    if (!src.equals(transferSource)) {
                        seenSources.add(src);
                    }
                }
            }
            for (Identifier src : seenSources) {
                List<Power> powers = targetComp.getPowersFromSource(src);
                List<Power> topLevel = StolenPowerSlotManager.filterTopLevel(powers);
                if (!topLevel.isEmpty()) {
                    toSteal.put(src, topLevel);
                }
            }
        }

        if (toSteal.isEmpty()) return;

        boolean donorChanged = false;
        for (Map.Entry<Identifier, List<Power>> entry : toSteal.entrySet()) {
            Identifier originalSource = entry.getKey();
            List<Power> powers = entry.getValue();

            for (Power p : powers) {
                if (stripFromDonor) {
                    targetComp.removePower(p, originalSource);
                    donorChanged = true;
                }
                actorComp.addPower(p, transferSource);
            }

            StolenPowerSlotManager.registerSteal(actor.getUuid(), originalSource, powers);
        }

        if (donorChanged) targetComp.sync();
        actorComp.sync();
    }

    private void doGive(Entity actor, Entity target,
                        PowerHolderComponent actorComp, PowerHolderComponent targetComp) {
        Identifier selectedOriginal = StolenPowerSlotManager.getSelectedSource(actor);
        if (selectedOriginal == null) return;

        List<Power> packagePowers = StolenPowerSlotManager.getPowersForSource(actor, selectedOriginal);
        if (packagePowers.isEmpty()) return;

        boolean donorChanged = false;
        for (Power p : packagePowers) {
            if (stripFromDonor) {
                actorComp.removePower(p, transferSource);
                donorChanged = true;
            }
            targetComp.addPower(p, selectedOriginal);
        }

        if (stripFromDonor) {
            StolenPowerSlotManager.deregisterSource(actor.getUuid(), selectedOriginal);
        }

        if (donorChanged) actorComp.sync();
        targetComp.sync();
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.TRANSFER;
    }
}
