package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRouteContext;

/**
 * Provider-neutral boundary used by the always-available service routing
 * handler. Provider API classes are isolated behind conditional configuration.
 */
public interface TaskWorkflowRuntime {

    void buildTarget(ServiceTargetRouteContext context);
}
