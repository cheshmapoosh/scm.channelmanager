package ir.daneshrefah.scm.mq.jms.message;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;

import javax.jms.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaBytesMessage extends JakartaAbstractMessage implements BytesMessage {

    private final javax.jms.BytesMessage bytesMessage;

    @Override
    public long getBodyLength() throws JMSException {
        try {
            return bytesMessage.getBodyLength();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean readBoolean() throws JMSException {
        try {
            return bytesMessage.readBoolean();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte readByte() throws JMSException {
        try {
            return bytesMessage.readByte();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        try {
            return bytesMessage.readUnsignedByte();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short readShort() throws JMSException {
        try {
            return bytesMessage.readShort();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        try {
            return bytesMessage.readUnsignedShort();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public char readChar() throws JMSException {
        try {
            return bytesMessage.readChar();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readInt() throws JMSException {
        try {
            return bytesMessage.readInt();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readLong() throws JMSException {
        try {
            return bytesMessage.readLong();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float readFloat() throws JMSException {
        try {
            return bytesMessage.readFloat();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double readDouble() throws JMSException {
        try {
            return bytesMessage.readDouble();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readUTF() throws JMSException {
        try {
            return bytesMessage.readUTF();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        try {
            return bytesMessage.readBytes(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        try {
            return bytesMessage.readBytes(value, length);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        try {
            bytesMessage.writeBoolean(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeByte(byte value) throws JMSException {
        try {
            bytesMessage.writeByte(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeShort(short value) throws JMSException {
        try {
            bytesMessage.writeShort(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeChar(char value) throws JMSException {
        try {
            bytesMessage.writeChar(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeInt(int value) throws JMSException {
        try {
            bytesMessage.writeInt(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeLong(long value) throws JMSException {
        try {
            bytesMessage.writeLong(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFloat(float value) throws JMSException {
        try {
            bytesMessage.writeFloat(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeDouble(double value) throws JMSException {
        try {
            bytesMessage.writeDouble(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeUTF(String value) throws JMSException {
        try {
            bytesMessage.writeUTF(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        try {
            bytesMessage.writeBytes(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        try {
            bytesMessage.writeBytes(value, offset, length);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeObject(Object value) throws JMSException {
        try {
            bytesMessage.writeObject(value);
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() throws JMSException {
        try {
            bytesMessage.reset();
        } catch (javax.jms.JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message getMessage() {
        return bytesMessage;
    }
}
