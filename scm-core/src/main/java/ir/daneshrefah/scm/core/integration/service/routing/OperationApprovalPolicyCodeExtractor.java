package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationApprovalPolicyCodeExtractor {
    private static final String APPROVAL_POLICY_CODE = "approvalPolicyCode";

    private final ObjectMapper objectMapper;

    public String extract(Definition definition) {
        if (definition == null || StringUtils.isBlank(definition.getDetails())) {
            return null;
        }

        try {
            JsonNode details = objectMapper.readTree(definition.getDetails());
            JsonNode policyCode = details == null ? null : details.get(APPROVAL_POLICY_CODE);
            if (policyCode == null || policyCode.isNull()) {
                return null;
            }
            if (!policyCode.isTextual()) {
                throw new IllegalStateException("Invalid approval policy configuration in Definition.details: "
                        + APPROVAL_POLICY_CODE + " must be a string");
            }
            String code = policyCode.asText();
            return StringUtils.isBlank(code) ? null : code.trim();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Invalid JSON in Definition.details for operation approval policy configuration",
                    exception
            );
        }
    }
}
