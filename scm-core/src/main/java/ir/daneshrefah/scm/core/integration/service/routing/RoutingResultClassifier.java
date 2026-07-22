package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Component
public class RoutingResultClassifier {
    private static final Set<String> SUCCESS_VALUES = Set.of(
            "SUCCESS", "SUCCEEDED", "APPROVED", "COMPLETE", "COMPLETED",
            "SC_SUCCESS", "SUCCESSFULLY_PROCESSED", "00", "0"
    );
    private static final Set<String> FAILURE_VALUES = Set.of(
            "FAIL", "FAILED", "FAILURE", "REJECTED", "DECLINED",
            "SC_ERROR_BUSINESS", "SC_ERROR_VALIDATION", "CANCEL", "CANCELLED"
    );
    private static final Set<String> UNKNOWN_VALUES = Set.of(
            "UNKNOWN", "TIMEOUT", "TIMED_OUT", "PENDING", "PROCESSING",
            "UNREACHABLE", "CONNECTION_LOST", "AMBIGUOUS",
            "SC_PROCESSING", "SC_ERROR_SYSTEM", "SC_ERROR_UNREACHABLE_PROVIDER"
    );

    public Result classify(Object response, Throwable failure) {
        if (failure != null) {
            return isTemporary(failure) ? Result.TEMPORARY_OR_UNKNOWN : Result.DEFINITIVE_FAILURE;
        }
        MessageStatus status = response instanceof Message message ? message.getStatus()
                : response instanceof ScmFault fault ? fault.getStatus() : null;
        if (status != null) {
            return classifyStatus(status);
        }
        if (response instanceof Message) {
            return Result.TEMPORARY_OR_UNKNOWN;
        }
        if (response instanceof ScmFault) {
            return Result.DEFINITIVE_FAILURE;
        }
        if (response == null) {
            return Result.TEMPORARY_OR_UNKNOWN;
        }
        if (response instanceof JsonNode jsonNode) {
            return classifyJson(jsonNode);
        }
        if (response instanceof CharSequence text) {
            return classifyText(text.toString(), Result.TEMPORARY_OR_UNKNOWN);
        }
        return Result.SUCCESS;
    }

    private Result classifyStatus(MessageStatus status) {
        return switch (status) {
            case SC_SUCCESS, SUCCESSFULLY_PROCESSED -> Result.SUCCESS;
            case SC_PROCESSING, SC_ERROR_SYSTEM, SC_ERROR_UNREACHABLE_PROVIDER,
                 TIMEOUT, CONNECTION_NOT_ACCEPTED, CARD_ISSUER_OR_SWITCH_INOPERATIVE,
                 CARD_ISSUER_NOT_AVAILABLE, SYSTEM_DEFECT, SERVER_PROCESSING_ERROR,
                 TRANSACTION_PROCESSING_ERROR -> Result.TEMPORARY_OR_UNKNOWN;
            default -> Result.DEFINITIVE_FAILURE;
        };
    }

    private Result classifyJson(JsonNode response) {
        if (response == null || response.isNull() || response.isMissingNode()) {
            return Result.TEMPORARY_OR_UNKNOWN;
        }
        if (response.isTextual()) {
            return classifyText(response.asText(), Result.TEMPORARY_OR_UNKNOWN);
        }
        for (String field : new String[]{"successful", "success"}) {
            JsonNode success = response.get(field);
            if (success != null && success.isBoolean()) {
                return success.booleanValue() ? Result.SUCCESS : Result.DEFINITIVE_FAILURE;
            }
        }
        for (String field : new String[]{"status", "outcome", "result", "responseCode", "code"}) {
            JsonNode value = response.get(field);
            if (value == null || !value.isValueNode()) {
                continue;
            }
            Result classified = classifyText(value.asText(), null);
            return classified == null ? Result.TEMPORARY_OR_UNKNOWN : classified;
        }
        return Result.SUCCESS;
    }

    private Result classifyText(String value, Result fallback) {
        String normalized = normalize(value);
        if (normalized == null) {
            return fallback;
        }
        if (SUCCESS_VALUES.contains(normalized)) {
            return Result.SUCCESS;
        }
        if (FAILURE_VALUES.contains(normalized)) {
            return Result.DEFINITIVE_FAILURE;
        }
        if (UNKNOWN_VALUES.contains(normalized)) {
            return Result.TEMPORARY_OR_UNKNOWN;
        }
        return fallback;
    }

    private boolean isTemporary(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof AbstractBaseException baseException) {
                MessageStatus status = baseException.getExceptionInformation() == null
                        ? null
                        : baseException.getExceptionInformation().getMessageStatus();
                if (status != null) {
                    return classifyStatus(status) == Result.TEMPORARY_OR_UNKNOWN;
                }
            }
            if (current instanceof TimeoutException || current instanceof SocketTimeoutException
                    || current instanceof ConnectException || current instanceof SocketException
                    || current instanceof EOFException || current instanceof IOException) {
                return true;
            }
            String name = current.getClass().getName().toLowerCase(Locale.ROOT);
            if (name.contains("timeout") || name.contains("connect")
                    || name.contains("socket") || name.contains("unreachable")) {
                return true;
            }
            if (name.contains("business") || name.contains("validation")
                    || name.contains("rejected") || name.contains("declined")
                    || name.contains("invalidinput")) {
                return false;
            }
            current = current.getCause();
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    public enum Result { SUCCESS, TEMPORARY_OR_UNKNOWN, DEFINITIVE_FAILURE }
}
