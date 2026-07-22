package ir.daneshrefah.scm.core.integration.service.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSuccessChainStepDecisionPolicy implements ChainStepDecisionPolicy {
    public static final String CODE = "DEFAULT_SUCCESS";
    private final RoutingResultClassifier classifier;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public ChainStepDecision decide(ChainStepDecisionContext context) {
        return switch (classifier.classify(context.response(), context.failure())) {
            case SUCCESS -> ChainStepDecision.CONTINUE;
            case TEMPORARY_OR_UNKNOWN -> ChainStepDecision.RETRY_LATER;
            case DEFINITIVE_FAILURE -> ChainStepDecision.FAIL;
        };
    }
}
