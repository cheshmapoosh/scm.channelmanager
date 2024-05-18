package ir.daneshrefah.scm.notification.client.jms.message;

import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;
import lombok.RequiredArgsConstructor;

import javax.jms.Message;
import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaObjectMessage extends JakartaAbstractMessage implements ObjectMessage {

    private final javax.jms.ObjectMessage objectMessage;

    @Override
    public Message getMessage() {
        return objectMessage;
    }

    @Override
    public void setObject(Serializable object) throws JMSException {
        try {
            objectMessage.setObject(object);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Serializable getObject() throws JMSException {
        try {
            return objectMessage.getObject();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
