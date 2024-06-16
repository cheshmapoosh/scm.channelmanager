package ir.daneshrefah.scm.mq.jms;

import jakarta.jms.*;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakarataConnection implements Connection {

    private final javax.jms.Connection connection;

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        try {
            return new JakartaSession(connection.createSession(transacted, acknowledgeMode));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session createSession(int sessionMode) throws JMSException {
        try {
            return new JakartaSession(connection.createSession(sessionMode));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session createSession() throws JMSException {
        try {
            return new JakartaSession(connection.createSession());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getClientID() throws JMSException {
        try {
            return connection.getClientID();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        try {
            connection.setClientID(clientID);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        try {
            return new JakartaConnectionMetaData(connection.getMetaData());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {

    }

    @Override
    public void start() throws JMSException {
        try {
            connection.start();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws JMSException {
        try {
            connection.stop();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws JMSException {
        try {
            connection.close();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return null;
    }
}
