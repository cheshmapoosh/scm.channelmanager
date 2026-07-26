package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingCursor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;

public interface RoutingRecoveryPolicy {

    RoutingStrategy strategy();

    RoutingCursor resolveRetryCursor(
            RoutingPlan plan,
            RoutingExecutionSnapshot snapshot
    );
}
