package dev.overgrown.sync.factory.power.type.modify_label_render;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.Prioritized;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyLabelRenderPower extends Power implements Prioritized<ModifyLabelRenderPower> {

    public enum RenderMode {
        DEFAULT,
        HIDE_PARTIALLY,
        HIDE_COMPLETELY
    }

    private final Consumer<Entity> beforeParseAction;
    private final Consumer<Entity> afterParseAction;
    private final RenderMode renderMode;
    private final Text textTemplate;
    private final int tickRate;
    private final int priority;
    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    private Text cachedText = null;
    private Text previousText = null;
    private int tickCounter = 0;

    public ModifyLabelRenderPower(PowerType<?> type, LivingEntity entity,
                                  Consumer<Entity> beforeParseAction,
                                  Consumer<Entity> afterParseAction,
                                  RenderMode renderMode,
                                  Text textTemplate,
                                  int tickRate,
                                  int priority,
                                  Predicate<Entity> entityCondition,
                                  Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.beforeParseAction = beforeParseAction;
        this.afterParseAction = afterParseAction;
        this.renderMode = renderMode;
        this.textTemplate = textTemplate;
        this.tickRate = tickRate;
        this.priority = priority;
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;

        // Set up ticking if we have actions or text template
        if (beforeParseAction != null || afterParseAction != null || textTemplate != null) {
            this.setTicking(true);
        }

        // Initialize cached text immediately if template is provided
        if (textTemplate != null) {
            cachedText = parseText();
        }
    }

    @Override
    public void tick() {
        tickCounter++;

        if (tickCounter >= tickRate) {
            tickCounter = 0;

            // Execute before parse action
            if (beforeParseAction != null) {
                beforeParseAction.accept(entity);
            }

            // Parse text if template is provided
            if (textTemplate != null) {
                previousText = cachedText;
                cachedText = parseText();

                // Execute after parse action if text changed
                if (afterParseAction != null && !textEquals(previousText, cachedText)) {
                    afterParseAction.accept(entity);
                }
            }
        }
    }

    /**
     * Parses the text template, replacing selectors and other dynamic content
     */
    private Text parseText() {
        if (textTemplate == null) {
            return null;
        }

        try {
            // Create a command source from the entity to resolve selectors
            if (entity.getWorld().isClient) {
                // On client side, we can't parse selectors properly
                return textTemplate;
            }

            // Get the server command source for the entity
            ServerCommandSource source = entity.getCommandSource();

            // Recursively parse the text and all its siblings
            return parseTextRecursive(textTemplate, source, entity, 0);
        } catch (CommandSyntaxException e) {
            Sync.LOGGER.error("Failed to parse text template (syntax error): {}", e.getMessage());
            return textTemplate;
        } catch (Exception e) {
            Sync.LOGGER.error("Failed to parse text template: {}", e.getMessage());
            return textTemplate;
        }
    }

    /**
     * Recursively parses text content and rebuilds the text tree
     */
    private Text parseTextRecursive(Text text, ServerCommandSource source, Entity sender, int depth) throws CommandSyntaxException {
        if (depth > 100) {
            // Prevent stack overflow from circular references
            Sync.LOGGER.warn("Text parsing depth exceeded 100, stopping recursion");
            return text;
        }

        // Parse the content
        MutableText parsed = text.getContent().parse(source, sender, depth);

        // Apply the original style
        parsed.setStyle(text.getStyle());

        // Recursively parse and append all siblings
        for (Text sibling : text.getSiblings()) {
            parsed.append(parseTextRecursive(sibling, source, sender, depth + 1));
        }

        return parsed;
    }

    private boolean textEquals(Text a, Text b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Text.Serializer.toJson(a).equals(Text.Serializer.toJson(b));
    }

    /**
     * Determines whether this power should apply for a specific viewer.
     */
    public boolean shouldApplyForViewer(Entity viewer) {
        if (viewer == null) {
            return true; // If no viewer context, apply by default
        }

        // Check entity condition on the viewer
        if (entityCondition != null && !entityCondition.test(viewer)) {
            return false;
        }

        // Check bi-entity condition between viewer (actor) and holder (target)
        if (bientityCondition != null && !bientityCondition.test(new Pair<>(viewer, entity))) {
            return false;
        }

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
            nbt.putString("cachedText", Text.Serializer.toJson(cachedText));
        }
        if (previousText != null) {
            nbt.putString("previousText", Text.Serializer.toJson(previousText));
        }
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound nbt) {
            tickCounter = nbt.getInt("tickCounter");
            if (nbt.contains("cachedText", NbtElement.STRING_TYPE)) {
                cachedText = Text.Serializer.fromJson(nbt.getString("cachedText"));
            }
            if (nbt.contains("previousText", NbtElement.STRING_TYPE)) {
                previousText = Text.Serializer.fromJson(nbt.getString("previousText"));
            }
        }
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("modify_label_render"),
                new SerializableData()
                        .add("before_parse_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("after_parse_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("render_mode", SerializableDataTypes.STRING, "default")
                        .add("text", SerializableDataTypes.TEXT, null)
                        .add("tick_rate", SerializableDataTypes.INT, 20)
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, player) -> {
                    String renderModeStr = data.getString("render_mode").toUpperCase();
                    RenderMode renderMode;
                    try {
                        renderMode = RenderMode.valueOf(renderModeStr);
                    } catch (IllegalArgumentException e) {
                        Sync.LOGGER.warn("Invalid render_mode '{}', defaulting to DEFAULT", renderModeStr);
                        renderMode = RenderMode.DEFAULT;
                    }

                    return new ModifyLabelRenderPower(
                            type,
                            player,
                            data.get("before_parse_action"),
                            data.get("after_parse_action"),
                            renderMode,
                            data.get("text"),
                            data.getInt("tick_rate"),
                            data.getInt("priority"),
                            data.get("entity_condition"),
                            data.get("bientity_condition")
                    );
                })
                .allowCondition();
    }
}