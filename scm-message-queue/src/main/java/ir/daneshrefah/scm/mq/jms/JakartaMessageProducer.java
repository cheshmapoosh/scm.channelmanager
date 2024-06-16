package ir.daneshrefah.scm.mq.jms;

import ir.daneshrefah.scm.mq.jms.destination.DestinationHelper;
import ir.daneshrefah.scm.mq.jms.message.MessageHelper;
import jakarta.jms.*;
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
public class JakartaMessageProducer implements MessageProducer {

    @Getter
    private final javax.jms.MessageProducer messageProducer;

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        try {
            messageProducer.setDisableMessageID(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        try {
            return messageProducer.getDisableMessageID();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        try {
            messageProducer.setDisableMessageTimestamp(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        try {
            return messageProducer.getDisableMessageTimestamp();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        try {
            messageProducer.setDeliveryMode(deliveryMode);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        try {
            return messageProducer.getDeliveryMode();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        try {
            messageProducer.setPriority(defaultPriority);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPriority() throws JMSException {
        try {
            return messageProducer.getPriority();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        try {
            messageProducer.setTimeToLive(timeToLive);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getTimeToLive() throws JMSException {
        try {
            return messageProducer.getTimeToLive();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDeliveryDelay(long deliveryDelay) throws JMSException {
        try {
            messageProducer.setDeliveryDelay(deliveryDelay);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        try {
            return messageProducer.getDeliveryDelay();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Destination getDestination() throws JMSException {
        return null;
    }

    @Override
    public void close() throws JMSException {
        try {
            messageProducer.close();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Message message) throws JMSException {
        try {
            messageProducer.send(MessageHelper.mapJakartaMessageToJms(message));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        try {
            messageProducer.send(MessageHelper.mapJakartaMessageToJms(message), deliveryMode, priority, timeToLive);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        try {
            messageProducer.send(DestinationHelper.mapJakartaDestinationToJms(destination), MessageHelper.mapJakartaMessageToJms(message));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        try {
            messageProducer.send(DestinationHelper.mapJakartaDestinationToJms(destination), MessageHelper.mapJakartaMessageToJms(message),
                    deliveryMode, priority, timeToLive);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private javax.jms.CompletionListener extractJmsCompletionListener(CompletionListener completionListener) {
        if (completionListener instanceof JakartaCompletionListener) {
            return ((JakartaCompletionListener) completionListener).getCompletionListener();
        }
        return null;
    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {
        try {
            messageProducer.send(MessageHelper.mapJakartaMessageToJms(message), extractJmsCompletionListener(completionListener));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {
        try {
            messageProducer.send(MessageHelper.mapJakartaMessageToJms(message), deliveryMode, priority, timeToLive,
                    extractJmsCompletionListener(completionListener));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {
        try {
            messageProducer.send(DestinationHelper.mapJakartaDestinationToJms(destination), MessageHelper.mapJakartaMessageToJms(message),
                    extractJmsCompletionListener(completionListener));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {
        try {
            messageProducer.send(DestinationHelper.mapJakartaDestinationToJms(destination),
                    MessageHelper.mapJakartaMessageToJms(message), deliveryMode, priority, timeToLive,
                    extractJmsCompletionListener(completionListener));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
