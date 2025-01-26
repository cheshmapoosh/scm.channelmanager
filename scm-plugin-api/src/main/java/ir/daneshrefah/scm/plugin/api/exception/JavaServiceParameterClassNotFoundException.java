package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-24
 */
public class JavaServiceParameterClassNotFoundException extends AbstractJavaServiceException {

    private final String serviceCode;
    private final String parameterName;
    public JavaServiceParameterClassNotFoundException(String serviceCode, String parameterName, Throwable cause) {
        super("service [" + serviceCode + "] , parameter [" + parameterName + "] class not found.", cause, serviceCode);
        this.serviceCode = serviceCode;
        this.parameterName = parameterName;
    }


    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("serviceCode",serviceCode)
                .defineMessageParameter("parameterName",parameterName)
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}
