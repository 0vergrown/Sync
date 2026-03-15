package dev.overgrown.sync.factory.action.bientity.explode;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.function.Predicate;

public class ExplodeBiEntityAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor  = pair.getLeft();
        Entity target = pair.getRight();

        World world = actor.getWorld();
        if (world.isClient) return;

        // Build the block-destruction predicate the same way the entity action does.
        Predicate<CachedBlockPosition> indestructible = data.get("indestructible");
        if (data.isPresent("destructible")) {
            Predicate<CachedBlockPosition> destructible = data.get("destructible");
            indestructible = MiscUtil.combineOr(destructible.negate(), indestructible);
        }

        // Choose blast origin.
        Entity origin = data.getBoolean("at_target") ? target : actor;

        // actor is passed as the explosion entity, so:
        // - getOtherEntities() excludes actor from self-damage (vanilla behavior)
        // - kill/damage attribution goes to actor
        MiscUtil.createExplosion(
                world,
                actor, // responsible entity
                origin.getPos(), // blast centre
                data.getFloat("power"),
                data.getBoolean("create_fire"),
                data.get("destruction_type"),
                MiscUtil.getExplosionBehavior(world, indestructible)
        );
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("explode"),
                new SerializableData()
                        .add("power", SerializableDataTypes.FLOAT)
                        .add("destruction_type", ApoliDataTypes.BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
                        .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                        .add("create_fire", SerializableDataTypes.BOOLEAN, false)
                        .add("at_target", SerializableDataTypes.BOOLEAN, false),
                ExplodeBiEntityAction::action
        );
    }
}