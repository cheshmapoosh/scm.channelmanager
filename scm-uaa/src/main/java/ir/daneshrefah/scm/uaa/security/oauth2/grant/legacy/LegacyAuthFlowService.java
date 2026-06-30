package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import ir.daneshrefah.scm.uaa.observation.UaaObservation;
import org.springframework.stereotype.Service;

/**
 * Legacy auth flow facade kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Service
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyAuthFlowService {
    private final UaaObservation observation;

    public LegacyAuthFlowService(UaaObservation observation) {
        this.observation = observation;
    }

    public void started(String spanName, LegacyClientType clientType) {
        observation.operationStarted(context(spanName, clientType, "started", null));
    }

    public void completed(String spanName, LegacyClientType clientType) {
        observation.operationCompleted(context(spanName, clientType, "success", null));
    }

    public void failed(String spanName, LegacyClientType clientType, Throwable throwable) {
        observation.operationFailed(context(spanName, clientType, "failure", observation.safeErrorMessage(throwable)), throwable);
    }

    private UaaObservation.OperationContext context(
            String spanName,
            LegacyClientType clientType,
            String outcome,
            String reason
    ) {
        return new UaaObservation.OperationContext(
                spanName,
                "uaa.legacy",
                "legacy." + (clientType == null ? "unknown" : clientType.name().toLowerCase()) + "." + spanName,
                outcome,
                reason
        );
    }
}
