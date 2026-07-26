package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultRoutingDecisionPolicy implements RoutingDecisionPolicy {
    public static final String CODE = "DEFAULT_SUCCESS";

    private final RoutingResultClassifier classifier;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public RoutingDecisionResult decide(RoutingDecisionContext context) {
        RoutingResultClassifier.Result classified =
                classifier.classify(context.response(), context.failure());
        MessageStatus status = status(context.response());
        Object normalized = context.exchange() == null
                ? null
                : context.exchange().getProperty(
                        RoutingStepExecutor.NORMALIZED_OUTCOME_PROPERTY);
        return switch (classified) {
            case SUCCESS -> new RoutingDecisionResult(
                    RoutingDecision.SUCCESS,
                    status == null ? MessageStatus.SC_SUCCESS : status,
                    "ROUTING_SUCCESS",
                    null,
                    normalized == null ? null : String.valueOf(normalized)
            );
            case TEMPORARY_OR_UNKNOWN -> new RoutingDecisionResult(
                    RoutingDecision.RETRY_LATER,
                    status == null
                            ? MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER
                            : status,
                    "ROUTING_TEMPORARY_OR_UNKNOWN",
                    "The operation outcome is temporary or unknown",
                    normalized == null ? null : String.valueOf(normalized)
            );
            case DEFINITIVE_FAILURE -> new RoutingDecisionResult(
                    RoutingDecision.FAIL,
                    status == null ? MessageStatus.SC_ERROR_BUSINESS : status,
                    "ROUTING_DEFINITIVE_FAILURE",
                    "The operation outcome is a definitive failure",
                    normalized == null ? null : String.valueOf(normalized)
            );
        };
    }

    private MessageStatus status(Object response) {
        if (response instanceof Message message) {
            return message.getStatus();
        }
        if (response instanceof ScmFault fault) {
            return fault.getStatus();
        }
        return null;
    }
}
