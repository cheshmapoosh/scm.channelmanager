package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.token.OpenBankingToken;
import ir.daneshrefah.scm.core.integration.operation.handler.OperationTypeHandler;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationRouteBuilder extends RouteBuilder {
    private final OperationService operationService;
    private final PluginResolverService pluginResolverService;
    private final Map<String, PluginHandler> pluginHandlers;
    private final List<OperationTypeHandler> operationTypeHandlers;

    @Override
    public void configure() {
        List<Operation> operations = operationService.getAllOperations();
        operations.stream().filter(Operation::getActive).forEach(operation -> {
            if(operation.getName().trim().equals("PROCUREMENT_CORPORATE_BY_NATIONAL")){
                System.out.println("");
            }

            String routeId = "route-" + operation.getName();
            String fromUri = resolveFromUri(operation);
            RouteDefinition route = from(fromUri)
                    .routeId(routeId)
                    .setProperty(Message.OPERATION, constant(operation));

            defineExceptionHandler(route);
            applyMetrics(route, operation);

            route.process(exchange -> {
                if(operation.getName().trim().equals("XFER_COMMISSION")){
                    exchange.getIn().setHeader("commissionType", "31");
                }
            });

            List<PluginDetail> orderedBeforePluginDetails = pluginResolverService.resolveOrderedPluginDetails(operation, PluginPhase.BEFORE);
            applyBeforePlugins(route, orderedBeforePluginDetails, Map.of(Message.OPERATION, operation));
            buildTarget(route, operation);
            List<PluginDetail> orderedAfterPluginDetails = pluginResolverService.resolveOrderedPluginDetails(operation, PluginPhase.AFTER);
            applyAfterPlugins(route, orderedAfterPluginDetails, Map.of(Message.OPERATION, operation));
        });
    }

    private String resolveFromUri(Operation operation) {
        return "direct:" + operation.getName();
    }

    private void defineExceptionHandler(RouteDefinition route) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String routeId = exchange.getFromRouteId();
                    TraceUtils.getInstance().traceException(exchange,exception);
                    log.error("[Error Handler] Route {} threw: {}", routeId, exception.getMessage(), exception);
                    exchange.getIn().setBody(exception);
                }).to(Routes.GLOBAL_ERROR_HANDLER);
    }

    private void applyMetrics(RouteDefinition route, Operation operation) {

    }

    private void applyBeforePlugins(RouteDefinition route, List<PluginDetail> orderedBeforePluginDetails, Map<String, ?> properties) {
        if (orderedBeforePluginDetails == null) {
            return;
        }

        orderedBeforePluginDetails.forEach(detail -> {
            PluginHandler handler = Objects.requireNonNull(pluginHandlers.get(detail.getName()));
            handler.init(route, detail, properties);
            route.process(exchange -> {
                handler.handle(exchange, detail);
            });
        });
    }

    private void buildTarget(RouteDefinition route, Operation operation) {
        OperationTypeHandler handler = operationTypeHandlers.stream()
                .filter(h -> Objects.equals(operation.getType(), h.getOperationType()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Operation type not found"));
        handler.internalConfig(route, operation);
    }

    private void applyAfterPlugins(RouteDefinition route, List<PluginDetail> orderedAfterPluginDetails, Map<String, ?> properties) {
        if (orderedAfterPluginDetails == null) {
            return;
        }

        orderedAfterPluginDetails.forEach(detail -> {
            PluginHandler handler = Objects.requireNonNull(pluginHandlers.get(detail.getName()));
            handler.init(route, detail, properties);
            route.process(exchange -> {
                handler.handle(exchange, detail);
            });
        });
    }
}
