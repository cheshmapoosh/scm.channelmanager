package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public class NotificationBodyProcessException extends BaseNotificationException{

    private final String templateCode;
    public NotificationBodyProcessException(NotificationRequest request, String templateCode,Exception e) {
        super(request, "notification body could not process for template code : " + templateCode, e);
        this.templateCode = templateCode;
    }

    @Override
    public String getSource() {
        return templateCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("templateCode",getSource())
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
