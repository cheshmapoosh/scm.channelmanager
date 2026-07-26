package ir.daneshrefah.scm.core.integration.service.routing.customOperationApprovalPolicy;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.data.constant.ActionCode;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultRoutingDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KarpardazPolicy implements RoutingDecisionPolicy {
    public static final String CODE = "SUCCESSFUL";
    private final DefaultRoutingDecisionPolicy defaultPolicy;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public RoutingDecisionResult decide(RoutingDecisionContext context) {
        if (context.failure() != null) {
            return defaultPolicy.decide(context);
        }
        JsonNode requestBody = context.response() instanceof JsonNode node ? node : null;
        if (requestBody == null || requestBody.isNull() || requestBody.isMissingNode()) {
            return failure("KARPARDAZ_EMPTY_RESPONSE",
                    "Karpardaz response is empty");
        }

        JsonNode actionCodeNode = requestBody.get("actionCode");
        if (actionCodeNode == null || actionCodeNode.isNull()) {
            return failure("KARPARDAZ_ACTION_CODE_MISSING",
                    "Karpardaz response actionCode is missing");
        }

        ActionCode actionCode = ActionCode.findByCode(actionCodeNode.asText());
        if (Objects.nonNull(actionCode) && actionCode.getName().equals(CODE)) {
            return new RoutingDecisionResult(
                    RoutingDecision.SUCCESS,
                    MessageStatus.SC_SUCCESS,
                    "KARPARDAZ_SUCCESS",
                    null,
                    actionCode.getName()
            );
        }
        return failure("KARPARDAZ_UNSUCCESSFUL",
                "Karpardaz response is not successful");
    }

    private RoutingDecisionResult failure(String reasonCode, String reasonMessage) {
        return new RoutingDecisionResult(
                RoutingDecision.FAIL,
                MessageStatus.SC_ERROR_BUSINESS,
                reasonCode,
                reasonMessage,
                null
        );
    }
}
