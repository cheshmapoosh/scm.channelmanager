package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Getter
public class NoMatchRecordFoundException extends AbstractValidationException {

    private final String providerCode;

    public NoMatchRecordFoundException(String source) {
        this(source, null);
    }

    public NoMatchRecordFoundException(String providerCode, String source) {
        super(source, "no " + (null != providerCode ? providerCode : "local") + "[" + source + "] found.");
        this.providerCode = providerCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("providerCode",null != providerCode ? providerCode : "local")
                .defineMessageParameter("source",getSource())
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }

}
