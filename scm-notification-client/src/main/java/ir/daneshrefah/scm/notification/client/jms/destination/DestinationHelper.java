package ir.daneshrefah.scm.notification.client.jms.destination;

import javax.jms.Destination;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
public class DestinationHelper {

    public static JakartaDestination mapJmsDestinationToJakarta(Destination destination) {
        JakartaDestination result = null;
        return result;
    }

    public static Destination mapJakartaDestinationToJms(jakarta.jms.Destination destination) {
        if (destination instanceof JakartaDestination<?>) {
            return ((JakartaDestination) destination).getDestination();
        }
        Destination result = null;
        return result;
    }
}
