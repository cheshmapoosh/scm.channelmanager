package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.plugin.PluginAware;
import ir.daneshrefah.scm.core.services.operation.OperationService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OperationRouteBuilder extends RouteBuilder {
    private final OperationService operationService;
    private final Map<String, PluginAware> plugins;
    @Override
    public void configure() {
        List<Operation> operations = operationService.getAllOperations();

        for (Operation operation : operations) {
            String routeId = "route-" + operation.getCode();
            String fromUri = resolveFromUri(operation);

            defineExceptionHandler(operation);
            ProcessorDefinition<?> route = from(fromUri)
                    .routeId(routeId);
            route = applyMetrics(route, operation);
            route = applyTracing(route, operation);
            route = applyPluginsBefore(route, operation);
            route = buildTarget(route, operation);
            route = applyPluginsAfter(route, operation);
            route.log("end of route: " + routeId);
        }
    }


    private String resolveFromUri(Operation operation) {
        return "direct:" + operation.getCode();
    }

    private void defineExceptionHandler(Operation operation) {

    }

    private ProcessorDefinition<?> applyMetrics(ProcessorDefinition<?> route, Operation operation) {
        return route;
    }

    private ProcessorDefinition<?> applyTracing(ProcessorDefinition<?> route, Operation operation) {
        return route;
    }

    private ProcessorDefinition<?> applyPluginsBefore(ProcessorDefinition<?> route, Operation operation) {
        return null;
    }

    private ProcessorDefinition<?> buildTarget(ProcessorDefinition<?> route, Operation operation) {
        return null;
    }

    private ProcessorDefinition<?> applyPluginsAfter(ProcessorDefinition<?> route, Operation operation) {
        return null;
    }



}
