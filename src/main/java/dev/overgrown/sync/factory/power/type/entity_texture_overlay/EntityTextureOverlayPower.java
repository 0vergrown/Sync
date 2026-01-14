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

    private final Identifier textureLocation;
    private final boolean showFirstPerson;
    private final boolean useRenderingPowers;
    private final boolean renderOriginalModel;
    private final boolean renderPlayerOuterLayer;

    public EntityTextureOverlayPower(PowerType<?> type, LivingEntity entity,
                                     Identifier textureLocation, boolean showFirstPerson,
                                     boolean useRenderingPowers, boolean renderOriginalModel,
                                     boolean renderPlayerOuterLayer) {
        super(type, entity);
        this.textureLocation = textureLocation;
        this.showFirstPerson = showFirstPerson;
        this.useRenderingPowers = useRenderingPowers;
        this.renderOriginalModel = renderOriginalModel;
        this.renderPlayerOuterLayer = renderPlayerOuterLayer;
    }

    public Identifier getTextureLocation() {
        return textureLocation;
    }

    public boolean shouldShowFirstPerson() {
        return showFirstPerson;
    }

    public boolean shouldUseRenderingPowers() {
        return useRenderingPowers;
    }

    public boolean shouldRenderOriginalModel() {
        return renderOriginalModel;
    }

    public boolean shouldRenderPlayerOuterLayer() {
        return renderPlayerOuterLayer;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("entity_texture_overlay"),
                new SerializableData()
                        .add("texture_location", SerializableDataTypes.IDENTIFIER)
                        .add("show_first_person", SerializableDataTypes.BOOLEAN, false)
                        .add("use_rendering_powers", SerializableDataTypes.BOOLEAN, false)
                        .add("render_original_model", SerializableDataTypes.BOOLEAN, true)
                        .add("render_player_outer_layer", SerializableDataTypes.BOOLEAN, true),
                data -> (powerType, entity) -> new EntityTextureOverlayPower(
                        powerType,
                        entity,
                        data.getId("texture_location"),
                        data.getBoolean("show_first_person"),
                        data.getBoolean("use_rendering_powers"),
                        data.getBoolean("render_original_model"),
                        data.getBoolean("render_player_outer_layer")
                )
        ).allowCondition();
    }
}