package dev.overgrown.sync.power.type.modify_label_render;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModifyLabelRenderPowerType extends PowerType implements Prioritized<ModifyLabelRenderPowerType> {

    public enum RenderMode {
        DEFAULT,
        HIDE_PARTIALLY,
        HIDE_COMPLETELY
    }

    public static final TypedDataObjectFactory<ModifyLabelRenderPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("before_parse_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("after_parse_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("render_mode", SerializableDataType.enumValue(RenderMode.class), RenderMode.DEFAULT)
            .add("text", SerializableDataTypes.TEXT.optional(), Optional.empty())
            .add("tick_rate", SerializableDataTypes.INT, 20)
            .add("priority", SerializableDataTypes.INT, 0)
            .add("entity_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new ModifyLabelRenderPowerType(
            data.get("before_parse_action"),
            data.get("after_parse_action"),
            data.get("render_mode"),
            data.get("text"),
            data.get("tick_rate"),
            data.get("priority"),
            data.get("entity_condition"),
            data.get("bientity_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("before_parse_action", powerType.beforeParseAction)
            .set("after_parse_action", powerType.afterParseAction)
            .set("render_mode", powerType.renderMode)
            .set("text", powerType.textTemplate)
            .set("tick_rate", powerType.tickRate)
            .set("priority", powerType.priority)
            .set("entity_condition", powerType.viewerCondition)
            .set("bientity_condition", powerType.biEntityCondition)
    );

    private final Optional<EntityAction> beforeParseAction;
    private final Optional<EntityAction> afterParseAction;
    private final RenderMode renderMode;
    private final Optional<Text> textTemplate;
    private final int tickRate;
    private final int priority;
    private final Optional<EntityCondition> viewerCondition;
    private final Optional<BiEntityCondition> biEntityCondition;

    private Text cachedText = null;
    private Text previousText = null;
    private int tickCounter = 0;

    public ModifyLabelRenderPowerType(Optional<EntityAction> beforeParseAction,
                                      Optional<EntityAction> afterParseAction,
                                      RenderMode renderMode,
                                      Optional<Text> textTemplate,
                                      int tickRate, int priority,
                                      Optional<EntityCondition> viewerCondition,
                                      Optional<BiEntityCondition> biEntityCondition,
                                      Optional<EntityCondition> condition) {
        super(condition);
        this.beforeParseAction = beforeParseAction;
        this.afterParseAction = afterParseAction;
        this.renderMode = renderMode;
        this.textTemplate = textTemplate;
        this.tickRate = tickRate;
        this.priority = priority;
        this.viewerCondition = viewerCondition;
        this.biEntityCondition = biEntityCondition;

        if (beforeParseAction.isPresent() || afterParseAction.isPresent() || textTemplate.isPresent()) {
            setTicking();
        }

        textTemplate.ifPresent(t -> cachedText = parseText());
    }

    @Override
    public void serverTick() {
        tickCounter++;

        if (tickCounter >= tickRate) {
            tickCounter = 0;

            beforeParseAction.ifPresent(act -> act.accept(new EntityActionContext(getHolder())));

            if (textTemplate.isPresent()) {
                previousText = cachedText;
                cachedText = parseText();

                if (afterParseAction.isPresent() && !textEquals(previousText, cachedText)) {
                    afterParseAction.get().accept(new EntityActionContext(getHolder()));
                }
            }
        }
    }

    private Text parseText() {
        if (textTemplate.isEmpty()) return null;

        try {
            Entity entity = getHolder();
            if (entity.getWorld().isClient) return textTemplate.get();

            ServerCommandSource source = entity.getCommandSource();
            return parseTextRecursive(textTemplate.get(), source, entity, 0);
        } catch (CommandSyntaxException e) {
            Sync.LOGGER.error("Failed to parse text template (syntax error): {}", e.getMessage());
            return textTemplate.get();
        } catch (Exception e) {
            Sync.LOGGER.error("Failed to parse text template: {}", e.getMessage());
            return textTemplate.get();
        }
    }

    private Text parseTextRecursive(Text text, ServerCommandSource source, Entity sender, int depth) throws CommandSyntaxException {
        if (depth > 100) {
            Sync.LOGGER.warn("Text parsing depth exceeded 100, stopping recursion");
            return text;
        }

        MutableText parsed = text.getContent().parse(source, sender, depth);
        parsed.setStyle(text.getStyle());

        for (Text sibling : text.getSiblings()) {
            parsed.append(parseTextRecursive(sibling, source, sender, depth + 1));
        }

        return parsed;
    }

    private boolean textEquals(Text a, Text b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.getString().equals(b.getString());
    }

    public boolean shouldApplyForViewer(Entity viewer) {
        if (viewer == null) return true;

        if (viewerCondition.isPresent() && !viewerCondition.get().test(viewer)) return false;
        if (biEntityCondition.isPresent() && !biEntityCondition.get().test(viewer, getHolder())) return false;

        return true;
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    public Text getModifiedText() {
        return cachedText;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public NbtElement toTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("tickCounter", tickCounter);
        if (cachedText != null) {
            TextCodecs.CODEC.encodeStart(getHolder().getRegistryManager().getOps(net.minecraft.nbt.NbtOps.INSTANCE), cachedText)
                .resultOrPartial(Sync.LOGGER::warn)
                .ifPresent(elem -> nbt.put("cachedText", elem));
        }
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound nbt) {
            tickCounter = nbt.getInt("tickCounter");
            if (nbt.contains("cachedText")) {
                TextCodecs.CODEC.parse(getHolder().getRegistryManager().getOps(net.minecraft.nbt.NbtOps.INSTANCE), nbt.get("cachedText"))
                    .resultOrPartial(Sync.LOGGER::warn)
                    .ifPresent(t -> cachedText = t);
            }
        }
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.MODIFY_LABEL_RENDER;
    }
}
