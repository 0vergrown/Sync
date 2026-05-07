package dev.overgrown.sync.condition.type.bientity.command;

import dev.overgrown.sync.registry.SyncBiEntityConditionTypes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<CommandBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("actor_selector", SerializableDataTypes.STRING, "%a")
            .add("target_selector", SerializableDataTypes.STRING, "%t")
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new CommandBiEntityConditionType(
            data.get("command"),
            data.get("actor_selector"),
            data.get("target_selector"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("command", conditionType.command)
            .set("actor_selector", conditionType.actorSelector)
            .set("target_selector", conditionType.targetSelector)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final String command;
    private final String actorSelector;
    private final String targetSelector;
    private final Comparison comparison;
    private final int compareTo;

    public CommandBiEntityConditionType(String command, String actorSelector, String targetSelector,
                                        Comparison comparison, int compareTo) {
        this.command = command;
        this.actorSelector = actorSelector;
        this.targetSelector = targetSelector;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(BiEntityConditionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (actor == null || target == null) return false;

        UUID actorUUID = actor.getUuid();
        UUID targetUUID = target.getUuid();

        MinecraftServer server = actor.getWorld().getServer();
        if (server == null) return false;

        ServerCommandSource source = getCommandSource(actor, server);
        String resolved = command.replace(actorSelector, actorUUID.toString())
                .replace(targetSelector, targetUUID.toString());
        int[] output = new int[]{0};
        ServerCommandSource consumingSource = source.withReturnValueConsumer((successful, value) -> output[0] = value);
        server.getCommandManager().executeWithPrefix(consumingSource, resolved);
        return comparison.compare(output[0], compareTo);
    }

    @NotNull
    private static ServerCommandSource getCommandSource(Entity actor, MinecraftServer server) {
        boolean validOutput = !(actor instanceof ServerPlayerEntity sp) || sp.networkHandler != null;
        return new ServerCommandSource(
            Apoli.config.executeCommand.showOutput && validOutput ? actor : CommandOutput.DUMMY,
            actor.getPos(),
            actor.getRotationClient(),
            actor.getWorld() instanceof ServerWorld sw ? sw : null,
            Apoli.config.executeCommand.permissionLevel,
            actor.getName().getString(),
            actor.getDisplayName(),
            server,
            actor
        );
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncBiEntityConditionTypes.COMMAND;
    }
}
