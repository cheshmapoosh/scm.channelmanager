package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper;

import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CamelInvocationStepBuilder {
    private final List<CamelInvocationStep> steps = new ArrayList<>();

    private CamelInvocationStepBuilder() {
    }

    public static CamelInvocationStepBuilder create() {
        return new CamelInvocationStepBuilder();
    }

    public CamelInvocationStepBuilder add(Consumer<Exchange> before, Consumer<TryDefinition> call, Consumer<Exchange> after) {
        steps.add(new CamelInvocationStep() {
            @Override
            public void beforeStepRouteCalling(Exchange exchange) {
                if (Objects.nonNull(before)) {
                    before.accept(exchange);
                }
            }

            @Override
            public void callStepRoute(TryDefinition routeDefinition) {
                if (Objects.nonNull(call)) {
                    call.accept(routeDefinition);
                }
            }

            @Override
            public void afterStepRouteCalling(Exchange exchange) {
                if (Objects.nonNull(after)) {
                    after.accept(exchange);
                }
            }
        });
        return this;
    }

    public List<CamelInvocationStep> build() {
        return steps;
    }
}
