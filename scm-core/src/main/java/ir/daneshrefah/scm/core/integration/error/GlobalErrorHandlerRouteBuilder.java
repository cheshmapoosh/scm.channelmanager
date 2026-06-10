package ir.daneshrefah.scm.core.integration.error;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
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
public class GlobalErrorHandlerRouteBuilder extends RouteBuilder {

    private final PluginResolverService pluginResolverService;
    private final GlobalErrorHandler globalErrorHandler;
    private final Map<String, PluginHandler> pluginHandlers;
    private final CoreObservationTraceSupport observationTraceSupport;

    @Override
    public void configure() throws Exception {
        RouteDefinition route = from(Routes.GLOBAL_ERROR_HANDLER);
        route.process(exchange -> {
            Service service = exchange.getProperty(Message.SERVICE, Service.class);
            Operation operation = exchange.getProperty(Message.OPERATION, Operation.class);
            GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
            ChannelServiceAccess channelServiceAccess = exchange.getProperty(Message.CHANNEL_SERVICE_ACCESS, ChannelServiceAccess.class);
            if (operation == null
                    || gatewayChannel == null
                    || channelServiceAccess == null
                    || channelServiceAccess.getService() == null) {
                globalErrorHandler.handle(exchange);
            } else {
                List<PluginDetail> channelPluginDetails = pluginResolverService.resolveOrderedPluginDetails(gatewayChannel.getChannel());
                List<PluginDetail> operationAfterThrowingPluginDetails = pluginResolverService.resolveOrderedPluginDetails(operation, PluginPhase.AFTER_THROWING);
                List<PluginDetail> gatewayAfterThrowingPluginDetails = pluginResolverService.resolveOrderedPluginDetails(channelPluginDetails, channelServiceAccess.getService(), PluginPhase.AFTER_THROWING);
                boolean hasAnyCustomErrorHandlerPlugin = checkCustomErrorHandlerPlugin(operationAfterThrowingPluginDetails, gatewayAfterThrowingPluginDetails);
                if (hasAnyCustomErrorHandlerPlugin) {
                    Map<String, Object> properties = exchange.getIn().getHeaders();
                    properties.put(Message.OPERATION, operation);
                    properties.put(Message.SERVICE, service);
                    properties.put(Message.GATEWAY_CHANNEL, gatewayChannel);
                    invokeCustomErrorHandlerPlugin(route, operationAfterThrowingPluginDetails, properties);
                    invokeCustomErrorHandlerPlugin(route, gatewayAfterThrowingPluginDetails, properties);
                } else {
                    globalErrorHandler.handle(exchange);
                }
            }
            Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            observationTraceSupport.traceException(exchange, exception);
        }).to(Routes.GLOBAL_RESPONSE_HANDLER);
    }

    private void invokeCustomErrorHandlerPlugin(RouteDefinition route, List<PluginDetail> pluginDetails, Map<String, ?> properties) {
        pluginDetails.forEach(detail -> {
            PluginHandler handler = Objects.requireNonNull(pluginHandlers.get(detail.getName()));
            handler.init(route, detail, properties);
            route.process(exchange -> {
                handler.handle(exchange, detail);
            });
        });
    }

    private boolean checkCustomErrorHandlerPlugin(List<PluginDetail> operationAfterThrowingPluginDetails, List<PluginDetail> gatewayAfterThrowingPluginDetails) {
        if (Objects.nonNull(operationAfterThrowingPluginDetails) && !operationAfterThrowingPluginDetails.isEmpty()) {
            return true;
        }
        return Objects.nonNull(gatewayAfterThrowingPluginDetails) && !gatewayAfterThrowingPluginDetails.isEmpty();
    }

}
