package ir.daneshrefah.scm.notification.client.jms.destination;

import jakarta.jms.JMSException;
import jakarta.jms.TemporaryTopic;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class JakartaTemporaryTopic extends JakartaDestination<javax.jms.TemporaryTopic> implements TemporaryTopic {

    public JakartaTemporaryTopic(javax.jms.TemporaryTopic destination) {
        super(destination);
    }

    @Override
    public void delete() throws JMSException {
        try {
            getDestination().delete();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTopicName() throws JMSException {
        try {
            return getDestination().getTopicName();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
