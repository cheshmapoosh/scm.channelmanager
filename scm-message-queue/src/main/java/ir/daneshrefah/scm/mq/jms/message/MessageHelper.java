package ir.daneshrefah.scm.mq.jms.message;

import javax.jms.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class MessageHelper {

    public static JakartaMessage mapJmsMessageToJakarta(Message message) {
        JakartaMessage result = null;
        return result;
    }

    public static Message mapJakartaMessageToJms(jakarta.jms.Message message) {
        if (message instanceof JakartaAbstractMessage) {
            return ((JakartaAbstractMessage) message).getMessage();
        }
        Message result = null;
        return result;
    }

}
