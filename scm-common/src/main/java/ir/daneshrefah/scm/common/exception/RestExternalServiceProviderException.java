package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.service.parameter.Response;
import lombok.Getter;

@Getter
public class RestExternalServiceProviderException extends AbstractServiceProviderException {

    private final Response responseCondition;
    private final String provider;
    private final String serviceCode;

    public RestExternalServiceProviderException(Response responseCondition, String provider, String serviceCode) {
        super(serviceCode, serviceCode, responseCondition.getResponseExceptionErrorCodeProperty(), responseCondition.getResponseExceptionErrorMessageProperty());
        this.responseCondition = responseCondition;
        this.provider = provider;
        this.serviceCode = serviceCode;
    }

}
