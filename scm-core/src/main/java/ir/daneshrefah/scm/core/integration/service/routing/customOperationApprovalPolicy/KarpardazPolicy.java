package ir.daneshrefah.scm.core.integration.service.routing.customOperationApprovalPolicy;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.data.constant.ActionCode;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultSuccessChainStepDecisionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KarpardazPolicy implements ChainStepDecisionPolicy {
    public static final String CODE = "SUCCESSFUL";
    private final DefaultSuccessChainStepDecisionPolicy defaultPolicy;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public ChainStepDecision decide(ChainStepDecisionContext context) {
        if (context.failure() != null) {
            return defaultPolicy.decide(context);
        }
        JsonNode requestBody = context.response() instanceof JsonNode node ? node : null;
        if (requestBody == null || requestBody.isNull() || requestBody.isMissingNode()) {
            return ChainStepDecision.FAIL;
        }

        JsonNode actionCodeNode = requestBody.get("actionCode");
        if (actionCodeNode == null || actionCodeNode.isNull()) {
            return ChainStepDecision.FAIL;
        }

        ActionCode actionCode = ActionCode.findByCode(actionCodeNode.asText());
        return Objects.nonNull(actionCode) && actionCode.getName().equals(CODE)
                ? ChainStepDecision.CONTINUE : ChainStepDecision.FAIL;
    }
}
