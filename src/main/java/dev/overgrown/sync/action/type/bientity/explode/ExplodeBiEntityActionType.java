package dev.overgrown.sync.action.type.bientity.explode;

import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<ExplodeBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", SerializableDataTypes.NON_NEGATIVE_FLOAT)
            .add("destruction_type", SerializableDataTypes.DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
            .add("indestructible", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("destructible", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("create_fire", SerializableDataTypes.BOOLEAN, false)
            .add("at_target", SerializableDataTypes.BOOLEAN, false)
            .add("indestructible_resistance", SerializableDataTypes.NON_NEGATIVE_FLOAT, 10.0F),
        data -> new ExplodeBiEntityActionType(
            data.get("power"),
            data.get("destruction_type"),
            data.get("indestructible"),
            data.get("destructible"),
            data.get("create_fire"),
            data.get("at_target"),
            data.get("indestructible_resistance")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
            .set("destruction_type", actionType.destructionType)
            .set("indestructible", actionType.indestructibleCondition)
            .set("destructible", actionType.destructibleCondition)
            .set("create_fire", actionType.createFire)
            .set("at_target", actionType.atTarget)
            .set("indestructible_resistance", actionType.indestructibleResistance)
    );

    private final float power;
    private final Explosion.DestructionType destructionType;
    private final Optional<BlockCondition> indestructibleCondition;
    private final Optional<BlockCondition> destructibleCondition;
    private final boolean createFire;
    private final boolean atTarget;
    private final float indestructibleResistance;

    public ExplodeBiEntityActionType(float power, Explosion.DestructionType destructionType,
                                     Optional<BlockCondition> indestructibleCondition,
                                     Optional<BlockCondition> destructibleCondition,
                                     boolean createFire, boolean atTarget, float indestructibleResistance) {
        this.power = power;
        this.destructionType = destructionType;
        this.indestructibleCondition = indestructibleCondition;
        this.destructibleCondition = destructibleCondition;
        this.createFire = createFire;
        this.atTarget = atTarget;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (actor == null || target == null) return;

        World world = actor.getWorld();
        if (world.isClient()) return;

        Predicate<BlockConditionContext> behaviorCondition = indestructibleCondition.orElse(null);
        if (destructibleCondition.isPresent()) {
            Predicate<BlockConditionContext> destNeg = destructibleCondition.get().negate();
            behaviorCondition = behaviorCondition == null ? destNeg : MiscUtil.combineOr(destNeg, behaviorCondition);
        }

        Entity origin = atTarget ? target : actor;

        MiscUtil.createExplosion(
            world,
            actor,
            origin.getPos(),
            power,
            createFire,
            destructionType,
            MiscUtil.createExplosionBehavior(behaviorCondition, indestructibleResistance)
        );
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.EXPLODE;
    }
}
