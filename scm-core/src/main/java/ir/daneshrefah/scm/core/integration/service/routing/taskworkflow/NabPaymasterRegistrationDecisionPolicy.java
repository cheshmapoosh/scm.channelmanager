package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.NabError;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultSuccessChainStepDecisionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NabPaymasterRegistrationDecisionPolicy implements ChainStepDecisionPolicy {
    public static final String CODE = "NAB_PAYMASTER_REGISTRATION";
    public static final String SUCCESS_ALREADY_APPLIED = "SUCCESS_ALREADY_APPLIED";
    private final DefaultSuccessChainStepDecisionPolicy defaultPolicy;

    @Override
    public String code() { return CODE; }

    @Override
    public ChainStepDecision decide(ChainStepDecisionContext context) {
        if (containsNabBusinessFailure(context.failure())) {
            return ChainStepDecision.FAIL;
        }
        if (context.failure() == null && alreadyApplied(context.response())) {
            return ChainStepDecision.CONTINUE;
        }
        return defaultPolicy.decide(context);
    }

    @Override
    public String normalizedOutcome(ChainStepDecisionContext context, ChainStepDecision decision) {
        return decision == ChainStepDecision.CONTINUE && alreadyApplied(context.response())
                ? SUCCESS_ALREADY_APPLIED
                : null;
    }

    private boolean containsNabBusinessFailure(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof NabError) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean alreadyApplied(Object response) {
        JsonNode value = response instanceof Message message
                ? message.getPayload()
                : response instanceof JsonNode jsonNode ? jsonNode : null;
        return value != null
                && SUCCESS_ALREADY_APPLIED.equalsIgnoreCase(
                value.path("normalizedOutcome").asText());
    }
}
