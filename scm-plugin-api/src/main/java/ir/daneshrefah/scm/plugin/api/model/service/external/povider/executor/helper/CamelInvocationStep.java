package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper;

import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;


public interface CamelInvocationStep {
    void beforeStepRouteCalling(Exchange exchange);

    void callStepRoute(TryDefinition routeDefinition);

    void afterStepRouteCalling(Exchange exchange);

}
