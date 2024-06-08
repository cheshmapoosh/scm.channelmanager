package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public class NotificationTemplateNotFoundException extends BaseNotificationException{

    private final String requestedTemplateCode;
    public NotificationTemplateNotFoundException(NotificationRequest request, String requestedTemplateCode) {
        super(request, "No notification template found for : " + requestedTemplateCode, null);
        this.requestedTemplateCode = requestedTemplateCode;
    }

    @Override
    public String getSource() {
        return requestedTemplateCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("requestedTemplateCode",getSource())
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
