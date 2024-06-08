package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class ServiceNotFoundException extends BaseServiceException {

    public ServiceNotFoundException(String serviceCode) {
        super("no serviceCode[" + serviceCode + "] found.", null, serviceCode);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("serviceCode",serviceCode)
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
