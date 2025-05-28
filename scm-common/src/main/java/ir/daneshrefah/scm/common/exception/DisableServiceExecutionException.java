package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.ScmService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class DisableServiceExecutionException extends BaseServiceException {

    public DisableServiceExecutionException(ScmService service) {
        this("service is disabled: " + service.getCode(), service);
    }

    public DisableServiceExecutionException(String message, ScmService service) {
        super(message, null, service);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("serviceCode",getSource())
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
