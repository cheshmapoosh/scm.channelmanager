package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

@Component
public class ServiceTargetRoutingRegistry {
    private final EnumMap<RoutingStrategy, ServiceTargetRoutingHandler> handlers =
            new EnumMap<>(RoutingStrategy.class);

    public ServiceTargetRoutingRegistry(List<ServiceTargetRoutingHandler> handlers) {
        Objects.requireNonNull(handlers, "Routing handlers are required");
        handlers.forEach(this::register);
    }

    public ServiceTargetRoutingHandler getRequired(RoutingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Service routing strategy is required");
        }
        ServiceTargetRoutingHandler handler = handlers.get(strategy);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported service routing strategy: " + strategy);
        }
        return handler;
    }

    private void register(ServiceTargetRoutingHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Routing handler must not be null");
        }
        RoutingStrategy strategy = handler.strategy();
        if (strategy == null) {
            throw new IllegalArgumentException("Routing handler strategy must not be null: "
                    + handler.getClass().getName());
        }
        ServiceTargetRoutingHandler existing = handlers.putIfAbsent(strategy, handler);
        if (existing != null) {
            throw new IllegalStateException("Duplicate routing handlers for strategy " + strategy
                    + ": " + existing.getClass().getName() + " and " + handler.getClass().getName());
        }
    }
}
