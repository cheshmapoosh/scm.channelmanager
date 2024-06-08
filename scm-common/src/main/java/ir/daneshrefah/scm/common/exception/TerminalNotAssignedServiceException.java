package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class TerminalNotAssignedServiceException extends BaseServiceException {

    private final String terminalCode;

    public TerminalNotAssignedServiceException(Service service, String terminalCode) {
        super(String.format("service '%s' not assigned to terminal '%s'.", service.getCode(), terminalCode),null, service);
        this.terminalCode = terminalCode;
    }


    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("serviceCode",getSource())
                .defineMessageParameter("terminalCode",terminalCode)
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}
