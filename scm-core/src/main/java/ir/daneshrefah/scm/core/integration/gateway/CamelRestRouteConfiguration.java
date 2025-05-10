package ir.daneshrefah.scm.core.integration.gateway;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.integration.processor.FailoverStrategy;
import ir.daneshrefah.scm.core.services.gateway.ChannelServiceAccessService;
import ir.daneshrefah.scm.core.services.gateway.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.core.services.gateway.GatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.CircuitBreakerDefinition;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.Resilience4jConfigurationDefinition;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
//TODO SCMNEW-5: Add condition for load this bean
public class CamelRestRouteConfiguration extends RouteBuilder {
    private final GatewayService gatewayService;
    private final ChannelServiceAccessService channelServiceAccessService;
    private final ChannelServiceDefinitionService channelServiceDefinitionService;
    private final Resilience4jConfigurationDefinition defaultBreaker;
    @Value("${spring.application.name}")
    private String appName;

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("gateway-channel");
    private final Meter meter = GlobalOpenTelemetry.getMeter("gateway-channel");
    private final LongCounter requestCounter = meter.counterBuilder("gateway_requests_total")
            .setDescription("Total number of processed gateway requests")
            .setUnit("1")
            .build();

    @Override
    public void configure() throws Exception {
        //TODO SCMNEW-5: appName or channelCode?
        GatewayChannel gatewayChannel = gatewayService.findRestGatewayChannelByCode(appName);
        if (gatewayChannel == null) {
            return;
        }

        List<ChannelServiceAccess> channelServiceAccesses = channelServiceAccessService.findAllByChannel(gatewayChannel.getChannel());

        if (CollectionUtils.isEmpty(channelServiceAccesses)) {
            return;
        }

        RestConfigurationDefinition restConfigurationDefinition = restConfiguration()
                .component("servlet");

        String host = gatewayChannel.getHost();
        if (StringUtils.isNotEmpty(host)) {
            restConfigurationDefinition.host(host);
        }

        channelServiceAccesses.stream()
                .filter(channelServiceAccess -> CollectionUtils.isNotEmpty(channelServiceAccess.getService().getServiceOperations()))
                .forEach(channelServiceAccess -> {
                    Service service = channelServiceAccess.getService();
                    String serviceCode = service.getCode().trim();

                    URIBuilder uri = new URIBuilder()
                            .setScheme("rest:post")
                            .setPath(gatewayChannel.getPath())
                            .appendPath(serviceCode);

                    RestChannelServiceDefinition definition = channelServiceDefinitionService.findRestDefinition(channelServiceAccess, gatewayChannel);

                    if (definition != null) {
                        if (definition.getHttpMethod() != null) {
                            uri.setScheme("rest:" + definition.getHttpMethod().getValue().toLowerCase());
                        }
                        if (StringUtils.isNotEmpty(definition.getPath())) {
                            uri.setPath(gatewayChannel.getPath())
                                    .appendPath(serviceCode);
                        }
                    }
                    defineExceptionHandler(service);

                    ProcessorDefinition<?> route = from(uri.toString())
                            .routeId(serviceCode + "-route")
                            .setProperty(Message.SERVICE, constant(service));

                    route = applyMetrics(route, service);
                    route = applyTracing(route, service);

                    route = applyAuthentication(route, service);
                    route = applyAuthorization(route, service);

                    route = applyPluginsBefore(route, service);

                    if (service.useCircuitBreaker) {
                        ProcessorDefinition<CircuitBreakerDefinition> circuitBreakerDefinition = applyCircuitBreaker(route, service)
                                .process(addTraceHeadersWithBaggage());
                        route = buildTarget(circuitBreakerDefinition, service)
                                .onFallback()
                                .setBody(constant("{\"error\":\"fallback\"}"))
                                .end();
                    } else {
                        route = route
                                .process(addTraceHeadersWithBaggage());
                        route = buildTarget(route, service);
                    }

                    if (service.enablePlugins) {
                        route = applyPluginsAfter(route, service);
                    }

                });
    }

    private ProcessorDefinition<?> applyMetrics(ProcessorDefinition<?> route, Service service) {
        return route.process(exchange -> {
            requestCounter.add(1, io.opentelemetry.api.common.Attributes.of(
                    io.opentelemetry.api.common.AttributeKey.stringKey("service.id"), service.getCode()
            ));
            System.out.println("[Metrics] Counted request for " + service.getCode());
        });
    }

    private ProcessorDefinition<?> applyTracing(ProcessorDefinition<?> route, Service service) {
        return route.process(exchange -> {
            Span span = tracer.spanBuilder("route-" + service.getCode())
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();
            Scope scope = span.makeCurrent();
            exchange.setProperty("otelSpan", span);
            exchange.setProperty("otelScope", scope);
            System.out.println("[Tracing] Started span for " + service.getCode());
        });
    }

    private Processor addTraceHeadersWithBaggage() {
        return exchange -> {
            Span span = (Span) exchange.getProperty("otelSpan");
            if (span != null) {
                exchange.getIn().setHeader("traceparent", "00-" + span.getSpanContext().getTraceId() + "-" + span.getSpanContext().getSpanId() + "-01");

                // Add example custom baggage
                Baggage baggage = Baggage.current().toBuilder()
                        .put("gateway-id", "gateway-channel", BaggageEntryMetadata.create("propagation=unlimited"))
                        .build();

                baggage.forEach((key, entry) -> {
                    exchange.getIn().setHeader("baggage-" + key, entry.getValue());
                });
            }
        };
    }


    private ProcessorDefinition<?> applyAuthentication(ProcessorDefinition<?> route, Service service) {
        return route.process(exchange -> {
            Authentication auth = exchange.getIn().getHeader("org.springframework.security.core.Authentication", Authentication.class);
            if (auth == null || !auth.isAuthenticated()) {
                throw new SecurityException("Unauthenticated request");
            }
            System.out.println("[Auth] Authenticated principal: " + auth.getName());
        });
    }

    private ProcessorDefinition<?> applyAuthorization(ProcessorDefinition<?> route, Service service) {
        return route.process(exchange -> {
            Authentication auth = exchange.getIn().getHeader("org.springframework.security.core.Authentication", Authentication.class);
            boolean authorized = auth.getAuthorities().stream()
                    .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));

            if (!authorized) {
                throw new SecurityException("Unauthorized access to service: " + service.getCode());
            }
            System.out.println("[AuthZ] Authorized user: " + auth.getName());
        });
    }

    private ProcessorDefinition<CircuitBreakerDefinition> applyCircuitBreaker(ProcessorDefinition<?> route, Service service) {
        return route.circuitBreaker()
                .resilience4jConfiguration()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .end();
    }

    private <T extends ProcessorDefinition<T>> T buildTarget(ProcessorDefinition<T> route, Service service) {
        if (Objects.equals(RoutingStrategy.FIRST, service.getRoutingStrategy())) {
            String operationCode = service.getServiceOperations().get(0).getOperationCode();
            String url = operationCode;
            if (!StringUtils.contains(url, ':')) {
                url = "direct:" + operationCode;
            }
            return route.to(url);
        }

        if (Objects.equals(RoutingStrategy.FAIL_OVER, service.getRoutingStrategy())) {
            MulticastDefinition multicast = new MulticastDefinition()
                    .parallelProcessing(false)
                    .stopOnException("false");
            service.targets.forEach(multicast::to);
            return multicast;
        }

        throw new IllegalArgumentException("Unsupported routing strategy: " + service.getRoutingStrategy());
    }

    private void defineExceptionHandler(Service service) {
        onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String routeId = exchange.getFromRouteId();
                    System.err.println("[Error Handler] Route " + routeId + " threw: " + exception.getMessage());
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
    }

    private ProcessorDefinition<?> applyPluginsBefore(ProcessorDefinition<?> route, Service service) {
        return route.process(exchange -> {
            System.out.println("[Plugin Before] Processing route: " + service.getCode());
            // TODO: Execute before plugins here
        });
    }

    private ProcessorDefinition<?> applyPluginsAfter(ProcessorDefinition<?> route, Service service) {
        return route.process(exchange -> {
            Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            if (ex != null) {
                System.out.println("[Plugin AfterThrowing] Route: " + service.getCode() + " Exception: " + ex.getMessage());
            } else {
                System.out.println("[Plugin After] Successfully completed route: " + service.getCode());
            }
            // TODO: Execute after or afterThrowing plugins here

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
    }

}
