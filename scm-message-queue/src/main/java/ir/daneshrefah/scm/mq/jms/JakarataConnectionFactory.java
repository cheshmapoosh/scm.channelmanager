package ir.daneshrefah.scm.mq.jms;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class JakarataConnectionFactory implements ConnectionFactory {

//    TODO should define connection pool
    private final MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
    @Setter
    private String username;
    @Setter
    private String password;

    public void setQueueManager(String queueManagerName) throws JMSException {
        try {
            connectionFactory.setQueueManager(queueManagerName);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHostName(String hostname) {
        connectionFactory.setHostName(hostname);
    }

    public void setPort(int port) throws JMSException {
        try {
            connectionFactory.setPort(port);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setChannel(String channelName) throws JMSException {
        try {
            connectionFactory.setChannel(channelName);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTransportType(int type) throws JMSException {
        try {
            connectionFactory.setTransportType(type);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection createConnection() throws JMSException {
        try {
            if (StringUtils.isNotEmpty(username)) {
                String p = StringUtils.isBlank(password) ? null : password;
                return new JakarataConnection(connectionFactory.createConnection(username, p));
            }
            return new JakarataConnection(connectionFactory.createConnection());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        try {
            return new JakarataConnection(connectionFactory.createConnection(userName, password));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JMSContext createContext() {
        return new JakartaJMSContext(connectionFactory.createContext());
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        return new JakartaJMSContext(connectionFactory.createContext(userName, password));
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        return new JakartaJMSContext(connectionFactory.createContext(userName, password, sessionMode));
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return new JakartaJMSContext(connectionFactory.createContext(sessionMode));
    }

}
