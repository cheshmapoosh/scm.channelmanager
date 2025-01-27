package ir.daneshrefah.scm.mq.jms;


import ir.daneshrefah.scm.mq.jms.message.JakartaMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
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
public class JakartaMessageConsumer implements MessageConsumer {

    @Getter
    private final javax.jms.MessageConsumer messageConsumer;

    @Override
    public String getMessageSelector() {
        try {
            return messageConsumer.getMessageSelector();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MessageListener getMessageListener() {
        try {
            return new JakartaMessageListener(messageConsumer.getMessageListener());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMessageListener(MessageListener messageListener) {
        try {
            javax.jms.MessageListener xMessageListener = message -> messageListener.onMessage(new JakartaMessage(message));
            messageConsumer.setMessageListener(xMessageListener);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message receive() {
        try {
            return new JakartaMessage(messageConsumer.receive());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message receive(long l) {
        try {
            return new JakartaMessage(messageConsumer.receive(l));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message receiveNoWait() {
        try {
            return new JakartaMessage(messageConsumer.receiveNoWait());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            messageConsumer.close();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
