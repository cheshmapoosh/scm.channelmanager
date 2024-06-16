package ir.daneshrefah.scm.mq.jms;

import ir.daneshrefah.scm.mq.jms.message.JakartaMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
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
public class JakartaMessageListener implements MessageListener {

    @Getter
    private final javax.jms.MessageListener messageListener;

    @Override
    public void onMessage(Message message) {
        if (message instanceof JakartaMessage) {
            messageListener.onMessage(((JakartaMessage) message).getMessage());
        } else {
            throw new RuntimeException("invalid message.");
        }
    }

}
