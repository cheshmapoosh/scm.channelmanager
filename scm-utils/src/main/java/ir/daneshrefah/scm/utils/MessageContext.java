package ir.daneshrefah.scm.utils;

import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.message.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageContext {

    @Getter
    private final Message message;

    private static final ThreadLocal<MessageContext> CONTEXT = new ThreadLocal<>();

    public static void init(Message message) {
        CONTEXT.set(new MessageContext(message));
    }

    public static MessageContext getCurrentContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public Optional<Message> getMessageOptional() {
        if (Objects.isNull(message)) {
            return Optional.empty();
        }
        return Optional.of(message);
    }

    public Authentication getAuthentication() {
        return getMessageOptional().map(msg -> msg.getHeader().getAuthentication()).orElse(null);
    }

}
