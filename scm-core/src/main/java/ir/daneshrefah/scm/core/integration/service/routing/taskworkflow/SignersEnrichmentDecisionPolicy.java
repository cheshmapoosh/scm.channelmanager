package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.NabError;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.service.routing.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignersEnrichmentDecisionPolicy implements RoutingDecisionPolicy {
    public static final String CODE = "SIGNERS_ENRICHMENT";
    public static final String SUCCESS_ALREADY_APPLIED = "SUCCESS_ALREADY_APPLIED";
    private final DefaultRoutingDecisionPolicy defaultPolicy;

    @Override
    public String code() { return CODE; }

    @Override
    public RoutingDecisionResult decide(RoutingDecisionContext context) {
        if (containsNabBusinessFailure(context.failure())) {
            return new RoutingDecisionResult(
                    RoutingDecision.FAIL,
                    MessageStatus.SC_ERROR_BUSINESS,
                    "SIGNERS_ENRICHMENT_FAILED",
                    "signers enricher failed",
                    null
            );
        }
        if (context.failure() == null && alreadyApplied(context.response())) {
            return new RoutingDecisionResult(
                    RoutingDecision.SUCCESS,
                    MessageStatus.SC_SUCCESS,
                    "SIGNERS_ENRICHMENT_ALREADY_APPLIED",
                    null,
                    SUCCESS_ALREADY_APPLIED
            );
        }
        return new RoutingDecisionResult(RoutingDecision.SUCCESS, MessageStatus.SC_SUCCESS, "", "", "");
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
