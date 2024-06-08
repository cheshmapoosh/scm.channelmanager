package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class TerminalServiceNotFoundException extends AbstractBaseException implements ExceptionSourceAware {


    private final String terminalCode;
    private final String serviceCode;
    public TerminalServiceNotFoundException(String terminalCode, String serviceCode) {
        super("service by code '" + serviceCode + "' with terminal code '" + terminalCode + "' not found.", null);
        this.terminalCode = terminalCode;
        this.serviceCode = serviceCode;
    }

    @Override
    public String getSource() {
        return serviceCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("serviceCode",getSource())
                .defineMessageParameter("terminalCode",terminalCode)
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
