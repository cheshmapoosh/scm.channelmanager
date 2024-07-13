package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.utils.string.StringUtils;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class ProviderUnreachableException extends AbstractExternalServiceException {

    public ProviderUnreachableException(String serviceCode, String providerCode, Throwable cause) {
        super(String.format(" provider [%s] for service [%s] is unreachable. ", providerCode, serviceCode) +
                        (null != cause ? StringUtils.isNotEmpty(cause.getMessage()) ? cause.getMessage() : cause.getClass().getName() : StringUtils.EMPTY), serviceCode, providerCode, cause);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("providerCode",getProviderCode())
                .defineMessageParameter("serviceCode",getServiceCode())
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }
}
