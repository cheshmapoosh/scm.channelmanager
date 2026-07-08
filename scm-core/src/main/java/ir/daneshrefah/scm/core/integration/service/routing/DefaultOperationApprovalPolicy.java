package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class DefaultOperationApprovalPolicy implements OperationApprovalPolicy {
    public static final String CODE = "DEFAULT";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean isApproved(OperationApprovalContext context) {
        JsonNode requestBody = context == null ? null : context.requestBody();
        return isSuccessful(requestBody);
    }

    private boolean isSuccessful(JsonNode requestBody) {
        if (requestBody == null || requestBody.isNull() || requestBody.isMissingNode()) {
            return false;
        }

        JsonNode successful = requestBody.get("successful");
        if (successful != null && successful.isBoolean()) {
            return successful.booleanValue();
        }

        JsonNode success = requestBody.get("success");
        if (success != null && success.isBoolean()) {
            return success.booleanValue();
        }

        JsonNode status = requestBody.get("status");
        return status != null
                && status.isValueNode()
                && ("SC_SUCCESS".equals(status.asText()) || "SUCCESS".equalsIgnoreCase(status.asText()));
    }
}
