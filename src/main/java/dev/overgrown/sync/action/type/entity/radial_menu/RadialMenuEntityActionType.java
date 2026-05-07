package dev.overgrown.sync.action.type.entity.radial_menu;

import dev.overgrown.sync.action.type.entity.radial_menu.payload.s2c.OpenRadialMenuPayload;
import dev.overgrown.sync.action.type.entity.radial_menu.utils.RadialMenuEntry;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RadialMenuEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RadialMenuEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entries", RadialMenuEntry.RADIAL_MENU_ENTRIES)
            .add("sprite_location", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty()),
        data -> new RadialMenuEntityActionType(data.get("entries"), data.get("sprite_location")),
        (a, sd) -> sd.instance()
            .set("entries", a.entries)
            .set("sprite_location", a.menuTexture)
    );

    private final List<RadialMenuEntry> entries;
    private final Optional<Identifier> menuTexture;

    public RadialMenuEntityActionType(List<RadialMenuEntry> entries, Optional<Identifier> menuTexture) {
        this.entries = entries;
        this.menuTexture = menuTexture;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (!(context.entity() instanceof ServerPlayerEntity player)) return;

        List<RadialMenuEntry> filtered = entries.stream()
            .filter(entry -> entry.getCondition().isEmpty() || entry.getCondition().get().test(player))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) return;

        ServerPlayNetworking.send(player, new OpenRadialMenuPayload(filtered, menuTexture));
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.RADIAL_MENU;
    }
}
