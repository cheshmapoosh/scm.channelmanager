package ir.daneshrefah.scm.mq.jms.message;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;

import javax.jms.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaTextMessage extends JakartaAbstractMessage implements TextMessage {

    private final javax.jms.TextMessage message;

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public void setText(String string) throws JMSException {
        try {
            message.setText(string);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getText() throws JMSException {
        try {
            return message.getText();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
