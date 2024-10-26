package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;

@Getter
public class AbstractServiceProviderException extends AbstractBaseException {


    private final String serviceProviderCode;
    private final String serviceCode;
    private final String providerErrorCode;
    private final String providerErrorMessage;

    public AbstractServiceProviderException(String serviceProviderCode, String serviceCode, String providerErrorCode, String providerErrorMessage) {
        super("error code : "+providerErrorCode+" throws on calling service code : "+serviceCode+" on provider code : "+providerErrorCode, null);
        this.serviceProviderCode = serviceProviderCode;
        this.serviceCode = serviceCode;
        this.providerErrorCode = providerErrorCode;
        this.providerErrorMessage = providerErrorMessage;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }

}
