package ir.daneshrefah.scm.core.integration.gateway;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;
import ir.daneshrefah.scm.core.services.gateway.ChannelServiceAccessService;
import ir.daneshrefah.scm.core.services.gateway.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.core.services.gateway.GatewayService;
import ir.daneshrefah.scm.core.services.plugin.PluginBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.Resilience4jConfigurationDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayChannelRouteBuilder extends RouteBuilder {
    private final GatewayService gatewayService;
    private final ChannelServiceAccessService channelServiceAccessService;
    private final ChannelServiceDefinitionService channelServiceDefinitionService;
    private final Resilience4jConfigurationDefinition defaultBreaker;
    private final List<ProtocolHandler> protocolHandlers;
    private final PluginBindingService pluginBindingService;


    @Value("${spring.application.name}")
    private String name;

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("gateway-channel");
//    private final Meter meter = GlobalOpenTelemetry.getMeter("gateway-channel");
//    private final LongCounter requestCounter = meter.counterBuilder("gateway_requests_total")
//            .setDescription("Total number of processed gateway requests")
//            .setUnit("1")
//            .build();

    @Override
    public void configure() throws Exception {
        if (CollectionUtils.isEmpty(protocolHandlers)) {
            throw new IllegalStateException("No any handler found");
        }
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(name);
        if (gatewayChannel == null) {
            throw new IllegalStateException("Gateway channel '" + name + "' not found");
        }

        List<ChannelServiceAccess> channelServiceAccesses = channelServiceAccessService.findAllByChannel(gatewayChannel.getChannel());

        if (CollectionUtils.isEmpty(channelServiceAccesses)) {
            throw new IllegalStateException("No any service found for channel " + gatewayChannel.getChannel().getCode() + " in " + gatewayChannel.getName() + " gateway");
        }

        ProtocolHandler protocolHandler = protocolHandlers.stream()
                .filter(h -> Objects.equals(gatewayChannel.getProtocolType(), h.getProtocol()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No handler for " + gatewayChannel.getName() + " gateway" +
                                " with " + gatewayChannel.getProtocolType() + " protocol"));
        ProtocolHandler.ProtocolConfigurer protocolConfigurer = protocolHandler.config(gatewayChannel, this);

        channelServiceAccesses.stream()
                .filter(channelServiceAccess -> CollectionUtils.isNotEmpty(channelServiceAccess.getService().getServiceOperations()))
                .forEach(channelServiceAccess -> {
                    Service service = channelServiceAccess.getService();


                    List<ChannelServiceDefinition> definitions =
                            channelServiceDefinitionService.findDefinitions(channelServiceAccess, gatewayChannel);

                    RouteDefinition route = protocolConfigurer.routeDefinition(channelServiceAccess, definitions)
                            .setProperty(Message.SERVICE, constant(service));

                    PluginBinding pluginBinding = pluginBindingService.findByChannel(gatewayChannel.getChannel());
                    defineExceptionHandler(route, pluginBinding);

                    applyMetrics(route, service);
                    applyTracing(route, service);
                    applyPluginsBefore(route, pluginBinding);
                    buildTarget(route, service);
                    applyPluginsAfter(route, pluginBinding);
                });
    }

    private void applyMetrics(ProcessorDefinition<?> route, Service service) {
//        return route.process(exchange -> {
//            requestCounter.add(1, io.opentelemetry.api.common.Attributes.of(
//                    io.opentelemetry.api.common.AttributeKey.stringKey("service.id"), service.getCode()
//            ));
//            System.out.println("[Metrics] Counted request for " + service.getCode());
//        });
    }

    private void applyTracing(ProcessorDefinition<?> route, Service service) {
        route.process(exchange -> {
            Span span = tracer.spanBuilder("route-" + service.getCode())
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();
            Scope scope = span.makeCurrent();
            exchange.setProperty("otelSpan", span);
            exchange.setProperty("otelScope", scope);
            log.debug("[Tracing] Started span for {}", service.getCode());
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


    private void applyAuthentication(ProcessorDefinition<?> route, Service service) {
//        route.process(exchange -> {
//            Authentication auth = exchange.getIn().getHeader("org.springframework.security.core.Authentication", Authentication.class);
//            if (auth == null || !auth.isAuthenticated()) {
//                log.error("Unauthenticated request for {}", service.getCode());
//                throw new SecurityException("Unauthenticated request");
//            }
//            log.error("[Auth] Authenticated principal: {}", auth.getName());
//        });
    }

    private void applyAuthorization(ProcessorDefinition<?> route, Service service) {
//        route.process(exchange -> {
//            Authentication auth = exchange.getIn().getHeader("org.springframework.security.core.Authentication", Authentication.class);
//            boolean authorized = auth.getAuthorities().stream()
//                    .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
//
//            if (!authorized) {
//                log.error("[AuthZ] access to service: {}", service.getCode());
//                throw new SecurityException("Unauthorized access to service: " + service.getCode());
//            }
//            log.debug("[AuthZ] Authorized user: {}", auth.getName());
//        });
    }


    private void buildTarget(RouteDefinition route, Service service) {
        Resilience4jConfigurationDefinition resilience4jConfigurationDefinition = new Resilience4jConfigurationDefinition();
        resilience4jConfigurationDefinition.setFailureRateThreshold("50");
        if (Objects.equals(RoutingStrategy.FIRST, service.getRoutingStrategy())) {
            String operationName = service.getServiceOperations().get(0).getOperationName();
            String url = resolveOperationUrl(operationName);
            if (service.useCircuitBreaker()) {
                route.circuitBreaker()
                        .resilience4jConfiguration(resilience4jConfigurationDefinition)
                        .to(url)
                        .onFallback()
                        .setBody(constant("{\"error\":\"fallback\"}"))
                        .end();
            } else {
                route.to(url);
            }
            return;
        }

        if (Objects.equals(RoutingStrategy.FAIL_OVER, service.getRoutingStrategy())) {
            MulticastDefinition multicast = route.multicast()
                    .parallelProcessing(false)
                    .stopOnException("false");
            service.getServiceOperations().forEach(serviceOperation -> {

                String operationName = serviceOperation.getOperationName();
                String url = resolveOperationUrl(operationName);
                if (service.useCircuitBreaker()) {
                    multicast.circuitBreaker()
                            .resilience4jConfiguration(resilience4jConfigurationDefinition)
                            .to(url)
                            .onFallback()
                            .setBody(constant("{\"error\":\"fallback\"}"))
                            .end();
                } else {
                    multicast.to(url).end();
                }

            });
            return;
        }

        throw new IllegalArgumentException("Unsupported routing strategy: " + service.getRoutingStrategy());
    }

    private String resolveOperationUrl(String operationName) {
        if (StringUtils.contains(operationName, ':')) {
            return operationName;
        }
        return "direct:" + operationName;
    }

    private void defineExceptionHandler(RouteDefinition route, PluginBinding pluginBinding) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String routeId = exchange.getFromRouteId();
                    log.debug("[Error Handler] Route {} threw: {}", routeId, exception.getMessage());
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

        if (pluginBinding == null) {
            return;
        }

//        List<PluginDefinition> afterThrowingAdvisors = pluginBinding.getAdvisors().stream()
//                .filter(advisor -> Objects.equals(PluginPhase.AFTER_THROWING, advisor.getPhase()))
//                .sorted(Comparator.comparingInt(PluginDefinition::getOrder))
//                .toList();
//        afterThrowingAdvisors.forEach(afterThrowingAdvisor -> {
//            route.process(exchange -> {
//                Map<String, ?> config = afterThrowingAdvisor.getConfig();
//                PluginAdvice pluginAdvice = afterThrowingAdvisor.getPluginAdvice();
//                if (pluginAdvice.supports(exchange, PluginPhase.AFTER_THROWING, config)) {
//                    pluginAdvice.afterThrowing(exchange, config);
//                }
//            });
//        });
    }

    private void applyPluginsBefore(RouteDefinition route, PluginBinding pluginBinding) {
        if (pluginBinding == null) {
            return;
        }

//        List<PluginDefinition> beforeAdvisors = pluginBinding.getAdvisors().stream()
//                .filter(advisor -> Objects.equals(PluginPhase.BEFORE, advisor.getPhase()))
//                .sorted(Comparator.comparingInt(PluginDefinition::getOrder))
//                .toList();
//        beforeAdvisors.forEach(beforeAdvisor -> {
//            route.process(exchange -> {
//                Map<String, ?> config = beforeAdvisor.getConfig();
//                PluginAdvice pluginAdvice = beforeAdvisor.getPluginAdvice();
//                if (pluginAdvice.supports(exchange, PluginPhase.BEFORE, config)) {
//                    pluginAdvice.before(exchange, config);
//                }
//
//            });
//        });

    }

    private void applyPluginsAfter(RouteDefinition route, PluginBinding pluginBinding) {
        if (pluginBinding == null) {
            return;
        }

//        List<PluginDefinition> afterAdvisors = pluginBinding.getAdvisors().stream()
//                .filter(advisor -> Objects.equals(PluginPhase.AFTER, advisor.getPhase()))
//                .sorted(Comparator.comparingInt(PluginDefinition::getOrder))
//                .toList();
//        afterAdvisors.forEach(afterAdvisor -> {
//            route.process(exchange -> {
//                Map<String, ?> config = afterAdvisor.getConfig();
//                PluginAdvice pluginAdvice = afterAdvisor.getPluginAdvice();
//                if (pluginAdvice.supports(exchange, PluginPhase.AFTER, config)) {
//                    pluginAdvice.after(exchange, config);
//                }
//                Span span = (Span) exchange.getProperty("otelSpan");
//                Scope scope = (Scope) exchange.getProperty("otelScope");
//                if (span != null) {
//                    span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
//                    span.end();
//                }
//                if (scope != null) {
//                    scope.close();
//                }
//            });
//        });
    }

}
