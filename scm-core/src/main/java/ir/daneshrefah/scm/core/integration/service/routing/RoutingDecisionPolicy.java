package ir.daneshrefah.scm.core.integration.service.routing;

public interface RoutingDecisionPolicy {

    String code();

    RoutingDecisionResult decide(RoutingDecisionContext context);
}
