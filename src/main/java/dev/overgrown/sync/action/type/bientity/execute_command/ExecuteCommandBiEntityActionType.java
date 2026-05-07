package dev.overgrown.sync.action.type.bientity.execute_command;

import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
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

public class ExecuteCommandBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<ExecuteCommandBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("actor_selector", SerializableDataTypes.STRING, "%a")
            .add("target_selector", SerializableDataTypes.STRING, "%t"),
        data -> new ExecuteCommandBiEntityActionType(
            data.get("command"),
            data.get("actor_selector"),
            data.get("target_selector")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("command", actionType.command)
            .set("actor_selector", actionType.actorSelector)
            .set("target_selector", actionType.targetSelector)
    );

    private final String command;
    private final String actorSelector;
    private final String targetSelector;

    public ExecuteCommandBiEntityActionType(String command, String actorSelector, String targetSelector) {
        this.command = command;
        this.actorSelector = actorSelector;
        this.targetSelector = targetSelector;
    }

    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (actor == null || target == null) return;

        UUID actorUUID = actor.getUuid();
        UUID targetUUID = target.getUuid();

        MinecraftServer server = actor.getWorld().getServer();
        if (server == null) return;

        ServerCommandSource source = getCommandSource(actor, server);
        String resolved = command.replace(actorSelector, actorUUID.toString())
                .replace(targetSelector, targetUUID.toString());
        server.getCommandManager().executeWithPrefix(source, resolved);
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
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.EXECUTE_COMMAND;
    }
}
