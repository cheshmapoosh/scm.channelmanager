package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class ServiceProviderActivationStatusException extends AbstractBaseException {

    public ServiceProviderActivationStatusException(String providerCode) {
        super("provider "+providerCode+" status is not active", null);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }
}
