package ir.daneshrefah.scm.core.integration.gateway;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.api.trace.Span;
import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import ir.daneshrefah.scm.core.services.gateway.ChannelServiceAccessService;
import ir.daneshrefah.scm.core.services.gateway.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.core.services.gateway.GatewayService;
import ir.daneshrefah.scm.core.services.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.utils.RouteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.Resilience4jConfigurationDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.tracing.ActiveSpanManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
    private final PluginResolverService pluginResolverService;
    private final Map<String, PluginHandler> pluginHandlers;

    @Value("${scm.app-name}")
    private String name;

//    private final Tracer tracer = GlobalOpenTelemetry.getTracer("gateway-channel");
//    private final Meter meter = GlobalOpenTelemetry.getMeter("gateway-channel");
//    private final LongCounter requestCounter = meter.counterBuilder("gateway_requests_total")
//            .setDescription("Total number of processed gateway requests")
//            .setUnit("1")
//            .build();

    @Override
    public void configure() throws Exception {
        if (CollectionUtils.isEmpty(protocolHandlers)) {
            log.error("No any handler found");
            throw new IllegalStateException("No any handler found");
        }
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(name);
        if (gatewayChannel == null) {
            log.error("Gateway channel '" + name + "' not found");
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

        List<PluginDetail> channelPluginDetails = pluginResolverService.resolveOrderedPluginDetails(gatewayChannel.getChannel());

        channelServiceAccesses.stream()
                .filter(channelServiceAccess -> CollectionUtils.isNotEmpty(channelServiceAccess.getService().getServiceOperations()))
                .forEach(channelServiceAccess -> {
                    Service service = channelServiceAccess.getService();


                    List<ChannelServiceDefinition> definitions =
                            channelServiceDefinitionService.findDefinitions(channelServiceAccess, gatewayChannel);
                    List<RouteDefinition> routes = protocolConfigurer.routeDefinition(channelServiceAccess, definitions);
                    routes.forEach(route -> {
                        route.setProperty(Message.SERVICE, constant(service));
                        route.setProperty(Message.CHANNEL_CODE, constant(channelServiceAccess.getChannel().getCode()));
                        route.setProperty(Message.CHANNEL_SERVICE_ACCESS, constant(channelServiceAccess));
                        route.setProperty(Message.GATEWAY_CHANNEL,constant(gatewayChannel));
                        route.setProperty(Message.GATEWAY_CHANNEL_PROTOCOL,constant(gatewayChannel.getProtocolType()));

                        defineExceptionHandler(route);
                        log.info(">>> exception handler defined succefully");
                        applyMetrics(route, service);
                        applyTracing(route, service);

                        List<PluginDetail> orderedBeforePluginDetails = pluginResolverService.resolveOrderedPluginDetails(channelPluginDetails,
                                channelServiceAccess.getService(),
                                PluginPhase.BEFORE);
                        applyBeforePlugins(route, orderedBeforePluginDetails);

                        buildTarget(route, service);

                        List<PluginDetail> orderedAfterPluginDetails = pluginResolverService.resolveOrderedPluginDetails(channelPluginDetails,
                                channelServiceAccess.getService(),
                                PluginPhase.AFTER);
                        applyAfterPlugins(route, orderedAfterPluginDetails);
                        route.to(Routes.GLOBAL_RESPONSE_HANDLER);
                    });
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
            TraceUtils.getInstance().traceBeforeRoute(exchange, service);
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
//        Resilience4jConfigurationDefinition resilience4jConfigurationDefinition = new Resilience4jConfigurationDefinition();
//        resilience4jConfigurationDefinition.setFailureRateThreshold("50");

        if (Objects.equals(RoutingStrategy.FIRST, service .getRoutingStrategy())) {
            ServiceOperation serviceOperation = service.getServiceOperations().get(0);
            route.setProperty(Message.SERVICE_OPERATION, constant(serviceOperation));
            String operationName = serviceOperation.getOperationName();
            String url = resolveOperationUrl(operationName);
//            if (service.useCircuitBreaker()) {
//                route
//                        .circuitBreaker()
//                        .resilience4jConfiguration(resilience4jConfigurationDefinition)
//                        .to(url)
//                        .onFallback()
//                        .setBody(constant("{\"error\":\"fallback\"}"))
//                        .end();
//            } else {
                route.to(url);
//            }
            return;
        }

        if (Objects.equals(RoutingStrategy.MULTI_OPERATION, service.getRoutingStrategy())) {
            /*
                ALL MULTIPLE ROUTES NAME ENDS WITH MDF OF OPERATION CODE
                SEE  routeDefinition() METHOD IN RestProtocolHandler CLASS
             */
            List<ServiceOperation> serviceOperations = service.getServiceOperations();
            String[] splitRouteName = route.getRouteId().split("-");
            ServiceOperation serviceOperation = serviceOperations
                    .stream()
                    .filter(o -> RouteUtils.getInstance().generateRouteUniqId(o.getOperationName()).equals(splitRouteName[splitRouteName.length - 1]))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No route found for " + route.getRouteId()));
            route.setProperty(Message.SERVICE_OPERATION, constant(serviceOperation));
            String operationName = serviceOperation.getOperationName();
            String url = resolveOperationUrl(operationName);
            route.to(url);
            return;
        }

        if (Objects.equals(RoutingStrategy.FAIL_OVER, service.getRoutingStrategy())) {
            MulticastDefinition multicast = route.multicast()
                    .parallelProcessing(false)
                    .stopOnException("false");
            service.getServiceOperations().forEach(serviceOperation -> {

                String operationName = serviceOperation.getOperationName();
                String url = resolveOperationUrl(operationName);
//                if (service.useCircuitBreaker()) {
//                    multicast
//                            .circuitBreaker()
//                            .resilience4jConfiguration(resilience4jConfigurationDefinition)
//                            .to(url)
//                            .onFallback()
//                            .setBody(constant("{\"error\":\"fallback\"}"))
//                            .end();
//                } else {
                    multicast.to(url).end();
//                }

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

    private void defineExceptionHandler(RouteDefinition route) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String routeId = exchange.getFromRouteId();
                    log.debug("[Error Handler] Route {} threw: {}", routeId, exception.getMessage());
                    TraceUtils.getInstance().traceException(exchange, exception);
                })
                .to(Routes.GLOBAL_ERROR_HANDLER);

    }

    private void applyBeforePlugins(RouteDefinition route, List<PluginDetail> orderedBeforePluginDetails) {
        if (orderedBeforePluginDetails == null) {
            return;
        }

        orderedBeforePluginDetails.forEach(definition -> {
            PluginHandler pluginHandler = Objects.requireNonNull(pluginHandlers.get(definition.getName()));
            route.process(exchange -> {
                pluginHandler.handle(exchange, definition);
            });
        });

    }

    private void applyAfterPlugins(RouteDefinition route, List<PluginDetail> orderedBeforePluginDetails) {
        route.process(exchange -> {
            Service service = exchange.getProperty(Message.SERVICE, Service.class);
            TraceUtils.getInstance().traceAfterRoute(exchange, service);
            ActiveSpanManager.endScope(exchange);
        });

        if (orderedBeforePluginDetails == null) {
            return;
        }

        orderedBeforePluginDetails.forEach(definition -> {
            PluginHandler pluginHandler = Objects.requireNonNull(pluginHandlers.get(definition.getName()));
            route.process(exchange -> {
                pluginHandler.handle(exchange, definition);
            });
        });
    }

}
