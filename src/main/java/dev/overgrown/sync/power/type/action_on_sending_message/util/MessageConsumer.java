package dev.overgrown.sync.power.type.action_on_sending_message.util;

import io.github.apace100.apoli.action.EntityAction;

import java.util.Optional;
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

    private final Optional<EntityAction> beforeAction;
    private final Optional<EntityAction> afterAction;
    private final Optional<String> replacement;
    private final boolean prevent;

    public MessageConsumer(String rawPattern,
                           Optional<EntityAction> beforeAction,
                           Optional<EntityAction> afterAction,
                           Optional<String> replacement,
                           boolean prevent) {
        this.rawPattern = rawPattern;
        String expanded = TranslationKeyResolver.expandPattern(rawPattern);
        Pattern p;
        try {
            p = Pattern.compile(expanded);
        } catch (PatternSyntaxException e) {
            p = Pattern.compile(Pattern.quote(expanded));
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

    public String applyReplacement(String message) {
        return replacement.map(r -> compiled.matcher(message).replaceAll(r)).orElse(message);
    }

    public String getRawPattern() {
        return rawPattern;
    }

    public Optional<EntityAction> getBeforeAction() {
        return beforeAction;
    }

    public Optional<EntityAction> getAfterAction() {
        return afterAction;
    }

    public Optional<String> getReplacement() {
        return replacement;
    }

    public boolean shouldPrevent() {
        return prevent;
    }
}
