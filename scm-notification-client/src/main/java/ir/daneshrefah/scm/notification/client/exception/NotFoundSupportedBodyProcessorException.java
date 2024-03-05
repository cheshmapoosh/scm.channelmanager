package ir.daneshrefah.scm.notification.client.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOTIFICATION_NOT_SUPPORTED_BODY_PROCESSOR_EXCEPTION;

public class NotFoundSupportedBodyProcessorException extends BaseNotificationException {
    private final String templateCode;

    public NotFoundSupportedBodyProcessorException(String templateCode) {
        super("notification body processor not found for template code : " + templateCode, null);
        this.templateCode = templateCode;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOTIFICATION_NOT_SUPPORTED_BODY_PROCESSOR_EXCEPTION;
    }

    @Override
    public String getSource() {
        return templateCode;
    }


}
