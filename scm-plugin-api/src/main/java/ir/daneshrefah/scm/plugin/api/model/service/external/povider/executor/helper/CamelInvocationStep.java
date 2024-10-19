package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper;

import org.apache.camel.model.TryDefinition;

@FunctionalInterface
public interface CamelInvocationStep {
    void call(TryDefinition routeDefinition);
}
