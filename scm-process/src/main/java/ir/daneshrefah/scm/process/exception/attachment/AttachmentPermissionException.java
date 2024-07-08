package ir.daneshrefah.scm.process.exception.attachment;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class AttachmentPermissionException extends AbstractProcessException {

    private final String attachment;
    private final String operation;

    public AttachmentPermissionException(String source, String message,String operation,String attachment) {
        super(source, message);
        this.attachment = attachment;
        this.operation = operation;
    }
    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("attachment", attachment)
                .defineMessageParameter("operation", operation)
                .buildWithStatus(MessageStatus.SC_UNAUTHORIZED);
    }
}
