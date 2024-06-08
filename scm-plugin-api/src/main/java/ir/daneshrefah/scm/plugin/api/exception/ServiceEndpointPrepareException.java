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
 * @since 2024-03-27
 */
public class ServiceEndpointPrepareException extends AbstractExternalServiceException {

    public ServiceEndpointPrepareException(String serviceCode, String providerCode, String serviceMetadata, Throwable cause) {
        super(String.format("error on extract endpoint uri with metadata [%s]. \n", serviceMetadata) +
                        (null != cause ? StringUtils.isNotEmpty(cause.getMessage()) ? cause.getMessage() : cause.getClass().getName() : StringUtils.EMPTY),
                serviceCode, providerCode, cause);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}
