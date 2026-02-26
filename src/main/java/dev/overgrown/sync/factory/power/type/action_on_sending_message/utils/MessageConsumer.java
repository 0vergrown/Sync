package dev.overgrown.sync.factory.power.type.action_on_sending_message.utils;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Holds a regex filter and optional entity actions.
 * <p>
 * {@code beforeAction} fires when the filter matches (message is about to be canceled).
 * {@code afterAction}  fires when the filter does NOT match (message passes through).
 */
public class MessageConsumer {

    private final String rawPattern;
    private final Pattern compiled;

    @Nullable
    private final Consumer<Entity> beforeAction;

    @Nullable
    private final Consumer<Entity> afterAction;

    public MessageConsumer(String rawPattern,
                           @Nullable Consumer<Entity> beforeAction,
                           @Nullable Consumer<Entity> afterAction) {
        this.rawPattern = rawPattern;
        Pattern p;
        try {
            p = Pattern.compile(rawPattern);
        } catch (PatternSyntaxException e) {
            // Fall back to a literal match so bad regex never crashes the server
            p = Pattern.compile(Pattern.quote(rawPattern));
        }
        this.compiled = p;
        this.beforeAction = beforeAction;
        this.afterAction = afterAction;
    }

    /** Returns {@code true} if {@code message} contains a match for the filter pattern. */
    public boolean matches(String message) {
        return compiled.matcher(message).find();
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
}