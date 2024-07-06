package ir.daneshrefah.scm.utils;

import ir.daneshrefah.scm.common.model.message.MessageInput;
import lombok.AccessLevel;
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
public class MessageInputContext {

    private static final ThreadLocal<MessageInput> CONTEXT = new ThreadLocal<>();

    public static void init(MessageInput messageInput) {
        CONTEXT.set(messageInput);
    }

    public static MessageInput getCurrentContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public Optional<MessageInput> getMessageInputOptional() {
        MessageInput messageInput = CONTEXT.get();
        if (Objects.isNull(messageInput)) {
            return Optional.empty();
        }
        return Optional.of(messageInput);
    }

//    public Authentication getAuthentication() {
//        return getMessageOptional().map(msg -> msg.getHeader().getAuthentication()).orElse(null);
//    }

    public String getCorrelationId() {
        return getMessageInputOptional().map(msg -> getCorrelationId()).orElse(null);
    }

    public String getTerminalCode() {
        return getMessageInputOptional().map(msg -> getTerminalCode()).orElse(null);
    }

    public String getClientId() {
        return getMessageInputOptional().map(msg -> getClientId()).orElse(null);
    }
}
