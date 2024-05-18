package ir.daneshrefah.scm.notification.client.jms;

import jakarta.jms.ConnectionMetaData;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;

import java.util.Enumeration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaConnectionMetaData implements ConnectionMetaData {

    private final javax.jms.ConnectionMetaData connectionMetaData;

    @Override
    public String getJMSVersion() throws JMSException {
        try {
            return connectionMetaData.getJMSVersion();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getJMSMajorVersion() throws JMSException {
        try {
            return connectionMetaData.getJMSMajorVersion();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getJMSMinorVersion() throws JMSException {
        try {
            return connectionMetaData.getJMSMinorVersion();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getJMSProviderName() throws JMSException {
        try {
            return connectionMetaData.getJMSProviderName();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProviderVersion() throws JMSException {
        try {
            return connectionMetaData.getProviderVersion();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getProviderMajorVersion() throws JMSException {
        try {
            return connectionMetaData.getProviderMajorVersion();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getProviderMinorVersion() throws JMSException {
        try {
            return connectionMetaData.getProviderMinorVersion();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Enumeration getJMSXPropertyNames() throws JMSException {
        try {
            return connectionMetaData.getJMSXPropertyNames();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
