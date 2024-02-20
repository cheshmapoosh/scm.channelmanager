package ir.daneshrefah.scm.notification.client.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public class NotificationProviderNotFoundException extends BaseNotificationException {

    private final String media;

    public NotificationProviderNotFoundException(String media) {
        super("No notification provider found for media: " + media, null);
        this.media = media;
    }

    @Override
    public String getSource() {
        return media;
    }

}
