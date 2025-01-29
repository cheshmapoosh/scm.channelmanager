//package ir.daneshrefah.scm.mq.jms;
//
//import ir.daneshrefah.scm.mq.jms.destination.DestinationHelper;
//import ir.daneshrefah.scm.mq.jms.message.JakartaBytesMessage;
//import ir.daneshrefah.scm.mq.jms.message.JakartaMapMessage;
//import ir.daneshrefah.scm.mq.jms.message.JakartaMessage;
//import ir.daneshrefah.scm.mq.jms.message.JakartaTextMessage;
//import jakarta.jms.*;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//
//import java.io.Serializable;
//
//@RequiredArgsConstructor
//public class JakartaQueueSession implements QueueSession {
//    @Getter
//    private final javax.jms.QueueSession queueSession;
//
//    @Override
//    public BytesMessage createBytesMessage() throws JMSException {
//        try {
//            return new JakartaBytesMessage(queueSession.createBytesMessage());
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public MapMessage createMapMessage() throws JMSException {
//        try {
//            return new JakartaMapMessage(queueSession.createMapMessage());
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public Message createMessage() throws JMSException {
//        try {
//            return new JakartaMessage(queueSession.createMessage());
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public ObjectMessage createObjectMessage() throws JMSException {
//        return null;
//    }
//
//    @Override
//    public ObjectMessage createObjectMessage(Serializable serializable) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public StreamMessage createStreamMessage() throws JMSException {
//        return null;
//    }
//
//    @Override
//    public TextMessage createTextMessage() throws JMSException {
//        try {
//            return new JakartaTextMessage(queueSession.createTextMessage());
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public TextMessage createTextMessage(String s) throws JMSException {
//        try {
//            return new JakartaTextMessage(queueSession.createTextMessage(s));
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public boolean getTransacted() throws JMSException {
//        return false;
//    }
//
//    @Override
//    public int getAcknowledgeMode() throws JMSException {
//        try {
//            return queueSession.getAcknowledgeMode();
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void commit() throws JMSException {
//        try {
//            queueSession.commit();
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void rollback() throws JMSException {
//        try {
//            queueSession.rollback();
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void close() throws JMSException {
//        try {
//            queueSession.close();
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void recover() throws JMSException {
//        try {
//            queueSession.recover();
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public MessageListener getMessageListener() throws JMSException {
//        try {
//            return new JakartaMessageListener(queueSession.getMessageListener());
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void setMessageListener(MessageListener messageListener) throws JMSException {
//    }
//
//    @Override
//    public void run() {
//        queueSession.run();
//    }
//
//    @Override
//    public MessageProducer createProducer(Destination destination) throws JMSException {
//        try {
//            return new JakartaMessageProducer(queueSession.createProducer(DestinationHelper.mapJakartaDestinationToJms(destination)));
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public MessageConsumer createConsumer(Destination destination) throws JMSException {
//        try {
//            return new JakartaMessageConsumer(queueSession.createConsumer(DestinationHelper.mapJakartaDestinationToJms(destination)));
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public MessageConsumer createConsumer(Destination destination, String s) throws JMSException {
//        try {
//            return new JakartaMessageConsumer(queueSession.createConsumer(DestinationHelper.mapJakartaDestinationToJms(destination), s));
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public MessageConsumer createConsumer(Destination destination, String s, boolean b) throws JMSException {
//        try {
//            return new JakartaMessageConsumer(queueSession.createConsumer(DestinationHelper.mapJakartaDestinationToJms(destination), s, b));
//        } catch (javax.jms.JMSException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public MessageConsumer createSharedConsumer(Topic topic, String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public MessageConsumer createSharedConsumer(Topic topic, String s, String s1) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public Queue createQueue(String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public Topic createTopic(String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public TopicSubscriber createDurableSubscriber(Topic topic, String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public TopicSubscriber createDurableSubscriber(Topic topic, String s, String s1, boolean b) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public MessageConsumer createDurableConsumer(Topic topic, String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public MessageConsumer createDurableConsumer(Topic topic, String s, String s1, boolean b) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public MessageConsumer createSharedDurableConsumer(Topic topic, String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public MessageConsumer createSharedDurableConsumer(Topic topic, String s, String s1) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public QueueReceiver createReceiver(Queue queue) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public QueueReceiver createReceiver(Queue queue, String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public QueueSender createSender(Queue queue) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public QueueBrowser createBrowser(Queue queue) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public QueueBrowser createBrowser(Queue queue, String s) throws JMSException {
//        return null;
//    }
//
//    @Override
//    public TemporaryQueue createTemporaryQueue() throws JMSException {
//        return null;
//    }
//
//    @Override
//    public TemporaryTopic createTemporaryTopic() throws JMSException {
//        return null;
//    }
//
//    @Override
//    public void unsubscribe(String s) throws JMSException {
//
//    }
//}
