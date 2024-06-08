package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.Locale;

@Component
public class UnknownHostExceptionResolver extends ExceptionResolver<UnknownHostException> {
    @Override
    public void resolve(Message message, UnknownHostException exception, Locale locale) {
        Error error = new Error(null, ErrorCodes.ERROR_CODE_UNKNOWN_HOST, "unknown host", exception);
        message.addError(error, MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }

    @Override
    public ResponseEntity<?> resolve(UnknownHostException exception, Locale locale) {
        return null;
    }
}
