package ir.daneshrefah.scm.notification.client.exception;

public class NotFoundSupportedBodyProcessorException extends BaseNotificationException {
    private final String templateCode;

    public NotFoundSupportedBodyProcessorException(String templateCode) {
        super("notification body processor not found for template code : " + templateCode, null);
        this.templateCode = templateCode;
    }

    @Override
    public String getSource() {
        return templateCode;
    }
}
