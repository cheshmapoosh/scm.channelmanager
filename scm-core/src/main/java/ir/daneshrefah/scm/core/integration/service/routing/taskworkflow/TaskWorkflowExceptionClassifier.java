package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

@Component
public class TaskWorkflowExceptionClassifier {

    public ExceptionResult classify(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (isUnknownTechnicalFailure(current)) {
                return ExceptionResult.UNKNOWN;
            }
            String simpleName = current.getClass().getSimpleName().toLowerCase(Locale.ROOT);
            if (simpleName.contains("business")
                    || simpleName.contains("validation")
                    || simpleName.contains("rejected")
                    || simpleName.contains("declined")
                    || simpleName.contains("invalidinput")) {
                return ExceptionResult.DEFINITIVE_FAILURE;
            }
            current = current.getCause();
        }
        return ExceptionResult.UNKNOWN;
    }

    private boolean isUnknownTechnicalFailure(Throwable error) {
        if (error instanceof TimeoutException
                || error instanceof SocketTimeoutException
                || error instanceof ConnectException
                || error instanceof SocketException
                || error instanceof EOFException
                || error instanceof IOException) {
            return true;
        }
        String name = error.getClass().getName().toLowerCase(Locale.ROOT);
        return name.contains("timeout")
                || name.contains("connect")
                || name.contains("socket")
                || name.contains("unreachable");
    }

    public enum ExceptionResult {
        DEFINITIVE_FAILURE,
        UNKNOWN
    }
}
