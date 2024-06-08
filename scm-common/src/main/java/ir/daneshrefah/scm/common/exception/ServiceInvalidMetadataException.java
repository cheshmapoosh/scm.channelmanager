package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class ServiceInvalidMetadataException extends BaseServiceException   {


    public ServiceInvalidMetadataException(String serviceCode, String message ) {
        super("service [" + serviceCode + "] has invalid metadata. " ,null, serviceCode);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("serviceCode",getSource())
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }

}
