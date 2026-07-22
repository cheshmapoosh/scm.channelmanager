package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RoutingEngineRegistry {
    private final Map<RoutingStrategy, RoutingEngine> engines;

    public RoutingEngineRegistry(List<RoutingEngine> engines) {
        EnumMap<RoutingStrategy, RoutingEngine> indexed = new EnumMap<>(RoutingStrategy.class);
        for (RoutingEngine engine : engines) {
            if (engine.strategy() == RoutingStrategy.TASK_WORKFLOW) {
                throw new IllegalStateException("TASK_WORKFLOW must not be registered as a routing engine");
            }
            if (indexed.putIfAbsent(engine.strategy(), engine) != null) {
                throw new IllegalStateException("Duplicate routing engine strategy=" + engine.strategy());
            }
        }
        this.engines = Map.copyOf(indexed);
    }

    public RoutingEngine getRequired(RoutingStrategy strategy) {
        RoutingEngine engine = engines.get(strategy);
        if (engine == null) {
            throw new IllegalStateException("No routing engine registered for strategy=" + strategy);
        }
        return engine;
    }
}
