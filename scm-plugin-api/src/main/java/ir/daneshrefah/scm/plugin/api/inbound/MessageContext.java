package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

}
