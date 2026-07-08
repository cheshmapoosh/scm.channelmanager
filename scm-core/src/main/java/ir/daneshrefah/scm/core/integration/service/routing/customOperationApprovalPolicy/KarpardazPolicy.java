package ir.daneshrefah.scm.core.integration.service.routing.customOperationApprovalPolicy;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.data.constant.ActionCode;
import ir.daneshrefah.scm.core.integration.service.routing.OperationApprovalContext;
import ir.daneshrefah.scm.core.integration.service.routing.OperationApprovalPolicy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KarpardazPolicy implements OperationApprovalPolicy {
    public static final String CODE = "SUCCESSFUL";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean isApproved(OperationApprovalContext context) {
        JsonNode requestBody = context == null ? null : context.requestBody();
        if (requestBody == null || requestBody.isNull() || requestBody.isMissingNode()) {
            return false;
        }

        JsonNode actionCodeNode = requestBody.get("actionCode");
        if (actionCodeNode == null || actionCodeNode.isNull()) {
            return false;
        }

        ActionCode actionCode = ActionCode.findByCode(actionCodeNode.asText());
        return Objects.nonNull(actionCode) && actionCode.getName().equals(CODE);
    }
}
