package ir.daneshrefah.scm.mq.jms.message;

import ir.daneshrefah.scm.mq.jms.destination.DestinationHelper;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import java.util.Enumeration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public abstract class JakartaAbstractMessage implements Message {

    public abstract javax.jms.Message getMessage();

    @Override
    public String getJMSMessageID() throws JMSException {
        try {
            return getMessage().getJMSMessageID();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        try {
            getMessage().setJMSMessageID(id);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        try {
            return getMessage().getJMSTimestamp();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        try {
            getMessage().setJMSTimestamp(timestamp);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        try {
            return getMessage().getJMSCorrelationIDAsBytes();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        try {
            getMessage().setJMSCorrelationIDAsBytes(correlationID);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        try {
            getMessage().setJMSCorrelationID(correlationID);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        try {
            return getMessage().getJMSCorrelationID();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        try {
            return DestinationHelper.mapJmsDestinationToJakarta(getMessage().getJMSReplyTo());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        try {
            getMessage().setJMSReplyTo(DestinationHelper.mapJakartaDestinationToJms(replyTo));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        try {
            return DestinationHelper.mapJmsDestinationToJakarta(getMessage().getJMSDestination());
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        try {
            getMessage().setJMSDestination(DestinationHelper.mapJakartaDestinationToJms(destination));
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        try {
            return getMessage().getJMSDeliveryMode();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        try {
            getMessage().setJMSDeliveryMode(deliveryMode);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        try {
            return getMessage().getJMSRedelivered();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        try {
            getMessage().setJMSRedelivered(redelivered);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getJMSType() throws JMSException {
        try {
            return getMessage().getJMSType();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        try {
            getMessage().setJMSType(type);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        try {
            return getMessage().getJMSExpiration();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        try {
            getMessage().setJMSExpiration(expiration);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        try {
            return getMessage().getJMSDeliveryTime();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
        try {
            getMessage().setJMSDeliveryTime(deliveryTime);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getJMSPriority() throws JMSException {
        try {
            return getMessage().getJMSPriority();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        try {
            getMessage().setJMSPriority(priority);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearProperties() throws JMSException {
        try {
            getMessage().clearProperties();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        try {
            return getMessage().propertyExists(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        try {
            return getMessage().getBooleanProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        try {
            return getMessage().getByteProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        try {
            return getMessage().getShortProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        try {
            return getMessage().getIntProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        try {
            return getMessage().getLongProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        try {
            return getMessage().getFloatProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        try {
            return getMessage().getDoubleProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        try {
            return getMessage().getStringProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        try {
            return getMessage().getObjectProperty(name);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        try {
            return getMessage().getPropertyNames();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        try {
            getMessage().setBooleanProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        try {
            getMessage().setByteProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        try {
            getMessage().setShortProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        try {
            getMessage().setIntProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        try {
            getMessage().setLongProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        try {
            getMessage().setFloatProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        try {
            getMessage().setDoubleProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        try {
            getMessage().setStringProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        try {
            getMessage().setObjectProperty(name, value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acknowledge() throws JMSException {
        try {
            getMessage().acknowledge();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearBody() throws JMSException {
        try {
            getMessage().clearBody();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T getBody(Class<T> c) throws JMSException {
        try {
            return getMessage().getBody(c);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isBodyAssignableTo(Class c) throws JMSException {
        try {
            return getMessage().isBodyAssignableTo(c);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
