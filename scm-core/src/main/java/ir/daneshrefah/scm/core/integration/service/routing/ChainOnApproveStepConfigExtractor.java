package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChainOnApproveStepConfigExtractor {
    private static final String EXECUTION_ORDER = "executionOrder";
    private static final String DECISION_POLICY = "decisionPolicy";
    private static final Set<String> SUPPORTED_FIELDS = Set.of(
            EXECUTION_ORDER,
            DECISION_POLICY
    );

    private final ObjectMapper objectMapper;

    public ChainOnApproveStepConfig extract(Service service, ServiceOperation serviceOperation) {
        Definition definition = serviceOperation == null ? null : serviceOperation.getDefinition();
        if (definition == null) {
            throw configurationException(service, serviceOperation, null, "definition",
                    "Each active CHAIN_ON_APPROVE service-operation must have a Definition");
        }
        if (StringUtils.isBlank(definition.getDetails())) {
            throw configurationException(service, serviceOperation, definition, "details",
                    "Definition.details is required for CHAIN_ON_APPROVE step configuration");
        }

        JsonNode details = parseDetails(service, serviceOperation, definition);
        if (!details.isObject()) {
            throw configurationException(service, serviceOperation, definition, "details",
                    "Definition.details must be a JSON object with integer executionOrder "
                            + "and optional string decisionPolicy");
        }
        validateSupportedFields(service, serviceOperation, definition, details);

        return new ChainOnApproveStepConfig(
                extractExecutionOrder(service, serviceOperation, definition, details),
                extractDecisionPolicy(service, serviceOperation, definition, details)
        );
    }

    private void validateSupportedFields(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode details
    ) {
        details.fieldNames().forEachRemaining(fieldName -> {
            if (!SUPPORTED_FIELDS.contains(fieldName)) {
                throw configurationException(service, serviceOperation, definition, fieldName,
                        "Unsupported CHAIN_ON_APPROVE step configuration field");
            }
        });
    }

    private JsonNode parseDetails(Service service, ServiceOperation serviceOperation, Definition definition) {
        try {
            return objectMapper.readTree(definition.getDetails());
        } catch (JsonProcessingException exception) {
            throw configurationException(service, serviceOperation, definition, "details",
                    "Definition.details must contain valid JSON", exception);
        }
    }

    private int extractExecutionOrder(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode details
    ) {
        JsonNode executionOrder = details.get(EXECUTION_ORDER);
        if (executionOrder == null || executionOrder.isNull()) {
            throw configurationException(service, serviceOperation, definition, EXECUTION_ORDER,
                    "executionOrder is required for every CHAIN_ON_APPROVE step");
        }
        if (!executionOrder.isIntegralNumber() || !executionOrder.canConvertToInt()) {
            throw configurationException(service, serviceOperation, definition, EXECUTION_ORDER,
                    "executionOrder must be an integer");
        }
        return executionOrder.intValue();
    }

    private String extractDecisionPolicy(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            JsonNode details
    ) {
        JsonNode decisionPolicy = details.get(DECISION_POLICY);
        if (decisionPolicy == null) {
            return DefaultSuccessChainStepDecisionPolicy.CODE;
        }
        if (!decisionPolicy.isTextual()) {
            throw configurationException(service, serviceOperation, definition, DECISION_POLICY,
                    "decisionPolicy must be a string when provided");
        }
        String code = decisionPolicy.asText();
        if (StringUtils.isBlank(code)) {
            throw configurationException(service, serviceOperation, definition, DECISION_POLICY,
                    "decisionPolicy must be non-blank when provided");
        }
        return code.trim();
    }

    private IllegalStateException configurationException(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            String fieldName,
            String message
    ) {
        return configurationException(service, serviceOperation, definition, fieldName, message, null);
    }

    private IllegalStateException configurationException(
            Service service,
            ServiceOperation serviceOperation,
            Definition definition,
            String fieldName,
            String message,
            Throwable cause
    ) {
        String text = "Invalid CHAIN_ON_APPROVE step configuration for serviceCode=" + serviceCode(service)
                + ", operationName=" + operationName(serviceOperation)
                + ", definitionId=" + definitionId(definition)
                + ", definitionName=" + definitionName(definition)
                + ", field=" + fieldName + ": " + message;
        return cause == null ? new IllegalStateException(text) : new IllegalStateException(text, cause);
    }

    private String serviceCode(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }

    private String operationName(ServiceOperation serviceOperation) {
        return serviceOperation == null ? "<null>" : String.valueOf(serviceOperation.getOperationName());
    }

    private String definitionId(Definition definition) {
        return definition == null ? "<null>" : String.valueOf(definition.getId());
    }

    private String definitionName(Definition definition) {
        return definition == null ? "<null>" : String.valueOf(definition.getName());
    }
}
