package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class HttpOperationFailedExceptionResolver extends ExceptionResolver<HttpOperationFailedException> {
    @Override
    public void resolve(Message message, HttpOperationFailedException exception, Locale locale) {
        Error error = new Error(null, ErrorCodes.ERROR_CODE_INVALID_REMOTE_RESPONSE, "Http Operation Failed Exception", exception);
        message.addError(error, MessageStatus.SC_ERROR_SYSTEM);
    }

    @Override
    public ResponseEntity<?> resolve(HttpOperationFailedException exception, Locale locale) {
        return null;
    }
}
