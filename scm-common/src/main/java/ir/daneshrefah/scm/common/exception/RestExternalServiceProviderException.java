package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import lombok.Getter;

@Getter
public class RestExternalServiceProviderException extends AbstractBaseException {

    private final Response responseCondition;
    private final String provider;
    private final String serviceCode;

    public RestExternalServiceProviderException(Response responseCondition, String provider, String serviceCode) {
        this(responseCondition, provider,serviceCode,"default rest exception", null);

    }

    public RestExternalServiceProviderException(Response responseCondition, String provider, String serviceCode, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.responseCondition = responseCondition;
        this.provider = provider;
        this.serviceCode = serviceCode;
    }


    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }

}
