package dev.overgrown.sync.factory.power.type.action_on_sending_message.utils;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A single message filter entry that can match messages and perform three operations:
 * <ul>
 *   <li><b>Actions</b>: {@code before_action} fires before processing, {@code after_action} fires after.</li>
 *   <li><b>Prevention</b>: When {@code prevent} is true, matching messages are blocked.</li>
 *   <li><b>Replacement</b>: When {@code replacement} is set, matched text is substituted via regex.</li>
 * </ul>
 */
public class MessageConsumer {

    private final String rawPattern;
    private final Pattern compiled;

    @Nullable
    private final Consumer<Entity> beforeAction;

    @Nullable
    private final Consumer<Entity> afterAction;

    @Nullable
    private final String replacement;

    private final boolean prevent;

    public MessageConsumer(String rawPattern,
                           @Nullable Consumer<Entity> beforeAction,
                           @Nullable Consumer<Entity> afterAction,
                           @Nullable String replacement,
                           boolean prevent) {
        this.rawPattern = rawPattern;
        Pattern p;
        try {
            p = Pattern.compile(rawPattern);
        } catch (PatternSyntaxException e) {
            p = Pattern.compile(Pattern.quote(rawPattern));
        }
        this.compiled = p;
        this.beforeAction = beforeAction;
        this.afterAction = afterAction;
        this.replacement = replacement;
        this.prevent = prevent;
    }

    public boolean matches(String message) {
        return compiled.pattern().equals(message) || compiled.matcher(message).find();
    }

    /**
     * Applies the regex replacement to the message. Returns the original message
     * if no replacement is configured or the pattern doesn't match.
     */
    public String applyReplacement(String message) {
        if (replacement == null) return message;
        return compiled.matcher(message).replaceAll(replacement);
    }

    public String getRawPattern() {
        return rawPattern;
    }

    @Nullable
    public Consumer<Entity> getBeforeAction() {
        return beforeAction;
    }

    @Nullable
    public Consumer<Entity> getAfterAction() {
        return afterAction;
    }

    @Nullable
    public String getReplacement() {
        return replacement;
    }

    public boolean shouldPrevent() {
        return prevent;
    }
}