package dev.overgrown.sync.power.type.entity_texture_overlay;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EntityTextureOverlayPowerType extends PowerType {

    public static final TypedDataObjectFactory<EntityTextureOverlayPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("wide_texture_location", SerializableDataTypes.IDENTIFIER)
            .add("slim_texture_location", SerializableDataTypes.IDENTIFIER)
            .add("show_first_person", SerializableDataTypes.BOOLEAN, false)
            .add("render_as_overlay", SerializableDataTypes.BOOLEAN, false)
            .add("red", SerializableDataTypes.FLOAT, 1.0F)
            .add("green", SerializableDataTypes.FLOAT, 1.0F)
            .add("blue", SerializableDataTypes.FLOAT, 1.0F)
            .add("alpha", SerializableDataTypes.FLOAT, 1.0F)
            .add("hide_cape", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new EntityTextureOverlayPowerType(
            data.get("wide_texture_location"),
            data.get("slim_texture_location"),
            data.get("show_first_person"),
            data.get("render_as_overlay"),
            data.get("red"),
            data.get("green"),
            data.get("blue"),
            data.get("alpha"),
            data.get("hide_cape"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("wide_texture_location", powerType.wideTexture)
            .set("slim_texture_location", powerType.slimTexture)
            .set("show_first_person", powerType.showFirstPerson)
            .set("render_as_overlay", powerType.renderAsOverlay)
            .set("red", powerType.red)
            .set("green", powerType.green)
            .set("blue", powerType.blue)
            .set("alpha", powerType.alpha)
            .set("hide_cape", powerType.hideCape)
    );

    private final Identifier wideTexture;
    private final Identifier slimTexture;
    private final boolean showFirstPerson;
    private final boolean renderAsOverlay;
    private final float red, green, blue, alpha;
    private final boolean hideCape;

    public EntityTextureOverlayPowerType(Identifier wideTexture, Identifier slimTexture,
                                         boolean showFirstPerson, boolean renderAsOverlay,
                                         float red, float green, float blue, float alpha,
                                         boolean hideCape,
                                         Optional<EntityCondition> condition) {
        super(condition);
        this.wideTexture = wideTexture;
        this.slimTexture = slimTexture;
        this.showFirstPerson = showFirstPerson;
        this.renderAsOverlay = renderAsOverlay;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.hideCape = hideCape;
    }

    public Identifier getWideTextureLocation() { return wideTexture; }
    public Identifier getSlimTextureLocation() { return slimTexture; }
    public boolean shouldShowFirstPerson() { return showFirstPerson; }
    public boolean shouldRenderAsOverlay() { return renderAsOverlay; }
    public float getRed() { return red; }
    public float getGreen() { return green; }
    public float getBlue() { return blue; }
    public float getAlpha() { return alpha; }
    public boolean shouldHideCape() { return hideCape; }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.ENTITY_TEXTURE_OVERLAY;
    }
}
