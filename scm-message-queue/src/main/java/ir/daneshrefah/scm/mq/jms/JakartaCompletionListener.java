package ir.daneshrefah.scm.mq.jms;

import ir.daneshrefah.scm.mq.jms.message.MessageHelper;
import jakarta.jms.CompletionListener;
import jakarta.jms.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaCompletionListener implements CompletionListener {

    @Getter
    private final javax.jms.CompletionListener completionListener;

    @Override
    public void onCompletion(Message message) {
        completionListener.onCompletion(MessageHelper.mapJakartaMessageToJms(message));
    }

    @Override
    public void onException(Message message, Exception exception) {
        completionListener.onException(MessageHelper.mapJakartaMessageToJms(message), exception);
    }

}
