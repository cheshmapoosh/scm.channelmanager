package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.ProviderErrorMapping;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class BaseServiceProviderException extends AbstractBaseException {


    private final ProviderErrorMapping providerErrorMapping = new ProviderErrorMapping();

    public BaseServiceProviderException(String serviceProviderCode, String serviceCode, List<String> providerErrorCode, List<String> providerErrorMessage) {
        super("error codes : "+providerErrorCode
              +" with messages : "+providerErrorMessage+"throws on calling service code : "
              +serviceCode+" on provider code : "+providerErrorCode, null);
        this.providerErrorMapping.setServiceProviderCode(serviceProviderCode);
        this.providerErrorMapping.setServiceCode(serviceCode);
        this.providerErrorMapping.setProviderErrorCode(providerErrorCode);
        this.providerErrorMapping.setProviderErrorMessage(providerErrorMessage);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }

}
