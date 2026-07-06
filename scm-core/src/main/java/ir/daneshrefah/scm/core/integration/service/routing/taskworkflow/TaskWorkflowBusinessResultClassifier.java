package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class TaskWorkflowBusinessResultClassifier {
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

    private final ObjectMapper objectMapper;

    public TaskWorkflowBusinessResultClassifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BusinessResult classify(Object response) {
        if (response == null) {
            return BusinessResult.UNKNOWN;
        }
        if (response instanceof Message message) {
            return classifyStatus(message.getStatus());
        }
        if (response instanceof ScmFault fault) {
            return classifyStatus(fault.getStatus());
        }
        if (response instanceof JsonNode jsonNode) {
            return classifyJson(jsonNode);
        }
        if (response instanceof CharSequence text) {
            return classifyText(text.toString());
        }
        return classifyJson(objectMapper.valueToTree(response));
    }

    private BusinessResult classifyStatus(MessageStatus status) {
        if (status == null) {
            return BusinessResult.UNKNOWN;
        }
        return switch (status) {
            case SC_SUCCESS, SUCCESSFULLY_PROCESSED -> BusinessResult.SUCCESS;
            case SC_PROCESSING, SC_ERROR_SYSTEM, SC_ERROR_UNREACHABLE_PROVIDER,
                 TIMEOUT, CONNECTION_NOT_ACCEPTED, CARD_ISSUER_OR_SWITCH_INOPERATIVE,
                 CARD_ISSUER_NOT_AVAILABLE, SYSTEM_DEFECT, SERVER_PROCESSING_ERROR,
                 TRANSACTION_PROCESSING_ERROR -> BusinessResult.UNKNOWN;
            default -> BusinessResult.FAILURE;
        };
    }

    private BusinessResult classifyJson(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return BusinessResult.UNKNOWN;
        }
        if (root.isTextual()) {
            return classifyText(root.asText());
        }
        JsonNode success = root.get("success");
        if (success != null && success.isBoolean()) {
            return success.booleanValue() ? BusinessResult.SUCCESS : BusinessResult.FAILURE;
        }
        for (String field : new String[]{"status", "outcome", "result", "responseCode", "code"}) {
            JsonNode value = root.get(field);
            if (value != null && value.isValueNode()) {
                BusinessResult result = classifyText(value.asText());
                if (result != BusinessResult.UNKNOWN || isExplicitUnknown(value.asText())) {
                    return result;
                }
            }
        }
        return BusinessResult.UNKNOWN;
    }

    private BusinessResult classifyText(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return BusinessResult.UNKNOWN;
        }
        if (SUCCESS_VALUES.contains(normalized)) {
            return BusinessResult.SUCCESS;
        }
        if (FAILURE_VALUES.contains(normalized)) {
            return BusinessResult.FAILURE;
        }
        return BusinessResult.UNKNOWN;
    }

    private boolean isExplicitUnknown(String value) {
        String normalized = normalize(value);
        return normalized != null && UNKNOWN_VALUES.contains(normalized);
    }

    private String normalize(String value) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    public enum BusinessResult {
        SUCCESS,
        FAILURE,
        UNKNOWN
    }
}
