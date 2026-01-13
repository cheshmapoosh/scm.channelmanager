package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.utils.RouteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@Slf4j
public class RestProtocolHandler implements ProtocolHandler {
    @Override
    public ProtocolType getProtocol() {
        return ProtocolType.REST;
    }

    @Override
    public ProtocolConfigurer config(GatewayChannel gatewayChannel, RouteBuilder routeBuilder) {
        RestConfigurationDefinition restConfigurationDefinition = routeBuilder.restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .corsAllowCredentials(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*")
                .corsHeaderProperty("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE")
                .corsHeaderProperty("Access-Control-Allow-Headers", "*")
                .corsHeaderProperty("Access-Control-Allow-Credentials", "true")
                .corsHeaderProperty("Access-Control-Expose-Headers", "*");
        String host = gatewayChannel.getHost();
        if (StringUtils.isNotEmpty(host)) {
            restConfigurationDefinition.host(host);
        }
        return new RestProtocolConfigurer(gatewayChannel, routeBuilder);
    }

    private record RestProtocolConfigurer(
            GatewayChannel gatewayChannel,
            RouteBuilder routeBuilder) implements ProtocolConfigurer {
        @Override
        public List<RouteDefinition> routeDefinition(ChannelServiceAccess channelServiceAccess, List<ChannelServiceDefinition> channelServiceDefinitions) {
            List<RouteDefinition> routeDefinitions = new ArrayList<>();
            Service service = channelServiceAccess.getService();
            channelServiceDefinitions.forEach(channelServiceDefinition -> {
                routeDefinitions.addAll(
                        switch (channelServiceDefinition.getType()) {
                            case REST -> createRestRouteDefinition(service, channelServiceDefinitions);
                            case REST_MULTIPLE -> createRestMultipleRouteDefinition(service, channelServiceDefinitions);
                            default -> Collections.emptyList();
                        }
                );
            });
            return routeDefinitions;
        }

        private List<RouteDefinition> createRestMultipleRouteDefinition(Service service, List<ChannelServiceDefinition> channelServiceDefinitions) {
            List<RouteDefinition> routeDefinitions = new ArrayList<>();
            channelServiceDefinitions.forEach(channelServiceDefinition -> {
                RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition = (RestMultipleChannelServiceDefinition) channelServiceDefinition;
                restMultipleChannelServiceDefinition.getMultiRouteDetails().forEach(multiRouteDetail ->
                        routeDefinitions.addAll(createRestRouteDefinition(service, multiRouteDetail)));
            });
            return routeDefinitions;
        }


        private List<RouteDefinition> createRestRouteDefinition(Service service, RestMultipleChannelServiceDefinition.MultiRouteDetail multiRouteDetail) {
            String serviceCode = service.getCode().trim();
            URIBuilder uri = createDefaultUri(gatewayChannel, serviceCode);
            RestChannelServiceDefinition definition = multiRouteDetail.getDefinition();
            if (definition != null) {
                if (definition.getMethod() != null) {
                    uri.setScheme("rest:" + definition.getMethod().getValue().toLowerCase());
                }
                if (StringUtils.isNotEmpty(definition.getPath())) {
                    uri.setPath(gatewayChannel.getPath())
                            .appendPath(definition.getPath());
                }
            }

            RouteDefinition routeDefinition = routeBuilder.from(uri.toString())
                    .routeId(serviceCode + "-route-" + RouteUtils.getInstance().generateRouteUniqId(multiRouteDetail.getOperationCode()));
            routeDefinition.setProperty(Message.CHANNEL_SERVICE_DEFINITION,constant(definition));
            return Collections.singletonList(routeDefinition);
        }


        private List<RouteDefinition> createRestRouteDefinition(Service service, List<ChannelServiceDefinition> channelServiceDefinitions) {
            String serviceCode = service.getCode().trim();
            URIBuilder uri = createDefaultUri(gatewayChannel, serviceCode);
            RestChannelServiceDefinition definition = null;
            if (CollectionUtils.isNotEmpty(channelServiceDefinitions)) {
                definition = channelServiceDefinitions.stream()
                        .filter(channelServiceDefinition ->
                                Objects.equals(channelServiceDefinition.getType(), ChannelServiceDefinitionType.REST))
                        .findFirst()
                        .map(RestChannelServiceDefinition.class::cast)
                        .orElse(null);
            }
            if (definition != null) {
                if (definition.getMethod() != null) {
                    uri.setScheme("rest:" + definition.getMethod().getValue().toLowerCase());
                }
                if (StringUtils.isNotEmpty(definition.getPath())) {
                    uri.setPath(gatewayChannel.getPath())
                            .appendPath(definition.getPath());
                }
            }

            RouteDefinition routeDefinition = routeBuilder.from(uri.toString())
                    .routeId(serviceCode + "-route");
            routeDefinition.setProperty(Message.CHANNEL_SERVICE_DEFINITION,constant(definition));
            return Collections.singletonList(routeDefinition);
        }

        private URIBuilder createDefaultUri(GatewayChannel gatewayChannel, String serviceCode) {
            log.info("createDefaultUri of service : " + serviceCode);
            return new URIBuilder()
                    .setScheme("rest:post")
                    .setPath(gatewayChannel.getPath())
                    .appendPath(serviceCode);
        }
    }
}
