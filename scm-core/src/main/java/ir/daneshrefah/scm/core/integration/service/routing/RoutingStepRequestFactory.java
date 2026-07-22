package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.camel.Exchange;

@FunctionalInterface
public interface RoutingStepRequestFactory {
    Object create(Exchange exchange, RoutingExecutionContext context);
}
