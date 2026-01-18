package dev.overgrown.sync.factory.power.type.entity_texture_overlay;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class EntityTextureOverlayPower extends Power {

    private final Identifier wideTexture;
    private final Identifier slimTexture;
    private final boolean showFirstPerson;

    public EntityTextureOverlayPower(PowerType<?> type, LivingEntity entity,
                                     Identifier wideTexture, Identifier slimTexture, boolean showFirstPerson) {
        super(type, entity);
        this.wideTexture = wideTexture;
        this.slimTexture = slimTexture;
        this.showFirstPerson = showFirstPerson;
    }

    public Identifier getWideTextureLocation() {
        return wideTexture;
    }

    public Identifier getSlimTextureLocation() {
        return slimTexture;
    }

    public boolean shouldShowFirstPerson() {
        return showFirstPerson;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("entity_texture_overlay"),
                new SerializableData()
                        .add("wide_texture_location", SerializableDataTypes.IDENTIFIER)
                        .add("slim_texture_location", SerializableDataTypes.IDENTIFIER)
                        .add("show_first_person", SerializableDataTypes.BOOLEAN, false),
                data -> (powerType, entity) -> new EntityTextureOverlayPower(
                        powerType,
                        entity,
                        data.getId("wide_texture_location"),
                        data.getId("slim_texture_location"),
                        data.getBoolean("show_first_person")
                )
        ).allowCondition();
    }
}