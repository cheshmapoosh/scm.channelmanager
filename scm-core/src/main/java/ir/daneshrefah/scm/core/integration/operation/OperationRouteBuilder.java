package ir.daneshrefah.scm.core.integration.operation;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationDefinitionType;
import ir.daneshrefah.scm.common.model.operation.RestConfigOperationDefinition;
import ir.daneshrefah.scm.common.model.plugin.PluginDefinition;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.plugin.PluginHandler;
import ir.daneshrefah.scm.core.services.operation.OperationService;
import ir.daneshrefah.scm.core.services.plugin.PluginResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationRouteBuilder extends RouteBuilder {
    private final OperationService operationService;
    private final PluginResolverService pluginResolverService;
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("operation");
    private final Map<String, PluginHandler> pluginHandlers;


    @Override
    public void configure() {
        List<Operation> operations = operationService.getAllOperations();
        for (Operation operation : operations) {
            String routeId = "route-" + operation.getName();
            String fromUri = resolveFromUri(operation);

            RouteDefinition route = from(fromUri)
                    .routeId(routeId)
                    .setProperty(Message.OPERATION, constant(operation));
            ;

            List<PluginDefinition> orderedAfterThrowingPluginDefinitions = pluginResolverService.resolveOrderedPluignDefinitions(operation, PluginPhase.AFTER_THROWING);
            defineExceptionHandler(route, orderedAfterThrowingPluginDefinitions);

            applyMetrics(route, operation);
            applyTracing(route, operation);

            List<PluginDefinition> orderedBeforePluginDefinitions = pluginResolverService.resolveOrderedPluignDefinitions(operation, PluginPhase.BEFORE);
            applyBeforePlugins(route, orderedBeforePluginDefinitions);

            buildTarget(route, operation);

            List<PluginDefinition> orderedAfterPluginDefinitions = pluginResolverService.resolveOrderedPluignDefinitions(operation, PluginPhase.AFTER);
            applyAfterPlugins(route, orderedAfterPluginDefinitions);
        }
    }


    private String resolveFromUri(Operation operation) {
        return "direct:" + operation.getName();
    }

    private void defineExceptionHandler(RouteDefinition route, List<PluginDefinition> orderedAfterThrowingPluginDefinitions) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String routeId = exchange.getFromRouteId();
                    log.error("[Error Handler] Route {} threw: {}", routeId, exception.getMessage(), exception);
                    Span span = (Span) exchange.getProperty("otelSpan");
                    Scope scope = (Scope) exchange.getProperty("otelScope");
                    if (span != null) {
                        span.recordException(exception);
                        span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR);
                        span.end();
                    }
                    if (scope != null) {
                        scope.close();
                    }
                });

        if (orderedAfterThrowingPluginDefinitions == null) {
            return;
        }

        orderedAfterThrowingPluginDefinitions.forEach(definition -> {
            PluginHandler handler = Objects.requireNonNull(pluginHandlers.get(definition.getName()));
            route.process(exchange -> {
                handler.handle(exchange, definition);
            });
        });
    }

    private void applyMetrics(RouteDefinition route, Operation operation) {

    }

    private void applyTracing(RouteDefinition route, Operation operation) {
        route.process(exchange -> {
            Span span = tracer.spanBuilder("operation-" + operation.getName())
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();
            Scope scope = span.makeCurrent();
            exchange.setProperty("otelSpan", span);
            exchange.setProperty("otelScope", scope);
            log.info("[Tracing] Started span for {}", operation.getName());
        });
    }

    private void applyBeforePlugins(RouteDefinition route, List<PluginDefinition> orderedBeforePluginDefinitions) {
        if (orderedBeforePluginDefinitions == null) {
            return;
        }

        orderedBeforePluginDefinitions.forEach(definition -> {
            PluginHandler handler = Objects.requireNonNull(pluginHandlers.get(definition.getName()));
            route.process(exchange -> {
                handler.handle(exchange, definition);
            });
        });
    }

    private void buildTarget(RouteDefinition route, Operation operation) {
        switch (operation.getType()) {
            case EXTERNAL_REST -> {
                RestConfigOperationDefinition restConfigOperationDefinition = operation.getDefinitions().stream()
                        .filter(operationDefinition -> Objects.equals(operationDefinition.getType(), OperationDefinitionType.REST_CONFIG))
                        .findFirst()
                        .map(RestConfigOperationDefinition.class::cast)
                        .orElseThrow(() -> new RuntimeException("Operation definition not found"));

                StringBuilder targetUrl = new StringBuilder("webclient:" + restConfigOperationDefinition.getUrl() +
                        "?method=" + restConfigOperationDefinition.getHttpMethod().getValue());
                Integer responseTimeout = restConfigOperationDefinition.getResponseTimeout();
                if (responseTimeout != null) {
                    targetUrl.append("&responseTimeout=").append(responseTimeout);
                }
                Integer connectTimeout = restConfigOperationDefinition.getConnectTimeout();
                if (connectTimeout != null) {
                    targetUrl.append("&connectTimeout=").append(connectTimeout);
                }
                Integer writeTimeout = restConfigOperationDefinition.getWriteTimeout();
                if (writeTimeout != null) {
                    targetUrl.append("&writeTimeout=").append(writeTimeout);
                }
                Boolean retryEnabled = restConfigOperationDefinition.getRetryEnabled();
                if (retryEnabled != null) {
                    targetUrl.append("&retryEnabled=").append(retryEnabled);
                }
                Integer maxAttempts = restConfigOperationDefinition.getMaxAttempts();
                if (maxAttempts != null) {
                    targetUrl.append("&maxAttempts=").append(maxAttempts);
                }
                Integer minBackoff = restConfigOperationDefinition.getMinBackoff();
                if (minBackoff != null) {
                    targetUrl.append("&minBackoff=").append(minBackoff);
                }
                Boolean wiretap = restConfigOperationDefinition.getWiretap();
                if (wiretap != null) {
                    targetUrl.append("&wiretap=").append(wiretap);
                }
                route.to(targetUrl.toString());
            }
            case BEAN -> {
                route.to("bean:" + operation.getPath());
            }
            case JAVA -> {
                try {
                    Class<?> processorType = ClassUtils.getClass(operation.getPath());
                    if (processorType.isAssignableFrom(Processor.class)) {
                        throw new RuntimeException("The type %s not instance of %s".formatted(operation.getPath(), Processor.class.getName()));
                    }
                    Processor processor = (Processor) ConstructorUtils.invokeConstructor(processorType);
                    route.process(processor);
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }

            }
            default -> throw new IllegalStateException("Unexpected operation type: " + operation.getType());
        }
    }

    private void applyAfterPlugins(RouteDefinition route, List<PluginDefinition> orderedAfterPluginDefinitions) {
        route.process(exchange -> {
            Span span = (Span) exchange.getProperty("otelSpan");
            Scope scope = (Scope) exchange.getProperty("otelScope");
            if (span != null) {
                span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
                span.end();
            }
            if (scope != null) {
                scope.close();
            }
        });

        if (orderedAfterPluginDefinitions == null) {
            return;
        }

        orderedAfterPluginDefinitions.forEach(definition -> {
            PluginHandler handler = Objects.requireNonNull(pluginHandlers.get(definition.getName()));
            route.process(exchange -> {
                handler.handle(exchange, definition);
            });
        });
    }
}
