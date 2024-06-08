package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.NoRouteToHostException;
import java.util.Locale;

@Component
public class NoRouteToHostExceptionResolver extends ExceptionResolver<NoRouteToHostException> {
    @Override
    public void resolve(Message message, NoRouteToHostException exception, Locale locale) {
        Error error = new Error(null, ErrorCodes.ERROR_CODE_HOST_UNREACHABLE, "no route to host", exception);
        message.addError(error, MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }

    @Override
    public ResponseEntity<?> resolve(NoRouteToHostException exception, Locale locale) {
        return null;
    }
}
