package ir.daneshrefah.scm.notification.client.jms.destination;

import jakarta.jms.JMSException;
import jakarta.jms.Topic;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class JakartaTopic extends JakartaDestination<javax.jms.Topic> implements Topic  {

    public JakartaTopic(javax.jms.Topic destination) {
        super(destination);
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
