package ir.daneshrefah.scm.notification.client.jms.destination;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class JakartaQueue extends JakartaDestination<javax.jms.Queue> implements Queue {

    public JakartaQueue(javax.jms.Queue destination) {
        super(destination);
    }

    @Override
    public String getQueueName() throws JMSException {
        try {
            return getDestination().getQueueName();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
