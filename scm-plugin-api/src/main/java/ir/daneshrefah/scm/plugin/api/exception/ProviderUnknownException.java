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
public class ProviderUnknownException extends AbstractExternalServiceException {

    private final String providerCode;
    public ProviderUnknownException(String serviceCode, String providerCode, Throwable cause) {
        super(String.format(" provider [%s] is unreachable. " + (null != cause ? cause.getMessage() : StringUtils.EMPTY),providerCode),
                serviceCode, providerCode, cause);
        this.providerCode = providerCode;
    }


    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("providerCode",providerCode)
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }
}
