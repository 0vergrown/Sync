package dev.overgrown.sync.factory.power.type;

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
    private final boolean renderAsOverlay;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final boolean hideCape;

    public EntityTextureOverlayPower(PowerType<?> type, LivingEntity entity,
                                     Identifier wideTexture, Identifier slimTexture,
                                     boolean showFirstPerson, boolean renderAsOverlay,
                                     float red, float green, float blue, float alpha,
                                     boolean hideCape) {
        super(type, entity);
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

    public Identifier getWideTextureLocation() {
        return wideTexture;
    }

    public Identifier getSlimTextureLocation() {
        return slimTexture;
    }

    public boolean shouldShowFirstPerson() {
        return showFirstPerson;
    }

    public boolean shouldRenderAsOverlay() {
        return renderAsOverlay;
    }

    public float getRed() {
        return red;
    }
    public float getGreen() {
        return green;
    }
    public float getBlue() {
        return blue;
    }
    public float getAlpha() {
        return alpha;
    }

    public boolean shouldHideCape() {
        return hideCape;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("entity_texture_overlay"),
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
                data -> (powerType, entity) -> new EntityTextureOverlayPower(
                        powerType,
                        entity,
                        data.getId("wide_texture_location"),
                        data.getId("slim_texture_location"),
                        data.getBoolean("show_first_person"),
                        data.getBoolean("render_as_overlay"),
                        data.getFloat("red"),
                        data.getFloat("green"),
                        data.getFloat("blue"),
                        data.getFloat("alpha"),
                        data.getBoolean("hide_cape")
                )
        ).allowCondition();
    }
}