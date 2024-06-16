package ir.daneshrefah.scm.mq.jms;

import ir.daneshrefah.scm.mq.jms.destination.*;
import ir.daneshrefah.scm.mq.jms.message.*;
import jakarta.jms.*;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaSession implements Session {

    private final javax.jms.Session session;

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        try {
            return new JakartaBytesMessage(session.createBytesMessage());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        try {
            return new JakartaMapMessage(session.createMapMessage());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message createMessage() throws JMSException {
        try {
            return new JakartaMessage(session.createMessage());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        try {
            return new JakartaObjectMessage(session.createObjectMessage());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        try {
            return new JakartaObjectMessage(session.createObjectMessage(object));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        try {
            return new JakartaStreamMessage(session.createStreamMessage());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        try {
            return new JakartaTextMessage(session.createTextMessage());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        try {
            return new JakartaTextMessage(session.createTextMessage(text));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getTransacted() throws JMSException {
        try {
            return session.getTransacted();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        try {
            return session.getAcknowledgeMode();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() throws JMSException {
        try {
            session.commit();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollback() throws JMSException {
        try {
            session.rollback();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws JMSException {
        try {
            session.close();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void recover() throws JMSException {
        try {
            session.recover();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        try {
            return new JakartaMessageListener(session.getMessageListener());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        if (listener instanceof JakartaMessageListener) {
            javax.jms.MessageListener messageListener = ((JakartaMessageListener) listener).getMessageListener();
            try {
                session.setMessageListener(messageListener);
            } catch (javax.jms.JMSException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("invalid message.");
        }
    }

    @Override
    public void run() {
        session.run();
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        try {
            return new JakartaMessageProducer(session.createProducer(DestinationHelper.mapJakartaDestinationToJms(destination)));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) throws JMSException {
        return null;
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        try {
            return new JakartaQueue(session.createQueue(queueName));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        try {
            return new JakartaTopic(session.createTopic(topicName));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return null;
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        return null;
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) throws JMSException {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        try {
            return new JakartaTemporaryQueue(session.createTemporaryQueue());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        try {
            return new JakartaTemporaryTopic(session.createTemporaryTopic());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unsubscribe(String name) throws JMSException {
        try {
            session.unsubscribe(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
