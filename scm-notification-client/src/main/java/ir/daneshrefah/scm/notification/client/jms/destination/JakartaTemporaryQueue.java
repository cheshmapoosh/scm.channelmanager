package ir.daneshrefah.scm.notification.client.jms.destination;

import jakarta.jms.JMSException;
import jakarta.jms.TemporaryQueue;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class JakartaTemporaryQueue extends JakartaDestination<javax.jms.TemporaryQueue> implements TemporaryQueue {

    public JakartaTemporaryQueue(javax.jms.TemporaryQueue destination) {
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
    public String getQueueName() throws JMSException {
        try {
            return getDestination().getQueueName();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
