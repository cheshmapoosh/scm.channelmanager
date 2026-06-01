package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.utils.RouteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        public List<InboundRouteDefinition> routeDefinition(RuntimeServicePlan servicePlan) {
            List<InboundRouteDefinition> routeDefinitions = new ArrayList<>();
            Service service = servicePlan.service();
            List<ChannelServiceDefinition> channelServiceDefinitions = servicePlan.routeDefinitions();
            if (channelServiceDefinitions == null || channelServiceDefinitions.isEmpty()) {
                return createRestRouteDefinition(service, null);
            }
            channelServiceDefinitions.forEach(channelServiceDefinition -> {
                if (channelServiceDefinition.getType() == null) {
                    return;
                }
                routeDefinitions.addAll(
                        switch (channelServiceDefinition.getType()) {
                            case INBOUND_ROUTE, REST -> createRestRouteDefinition(service, (RestChannelServiceDefinition) channelServiceDefinition);
                            case INBOUND_ROUTE_GROUP, REST_MULTIPLE -> createRestMultipleRouteDefinition(service, (RestMultipleChannelServiceDefinition) channelServiceDefinition);
                            default -> Collections.emptyList();
                        }
                );
            });
            if (routeDefinitions.isEmpty()) {
                return createRestRouteDefinition(service, null);
            }
            return routeDefinitions;
        }

        private List<InboundRouteDefinition> createRestMultipleRouteDefinition(Service service, RestMultipleChannelServiceDefinition definition) {
            List<InboundRouteDefinition> routeDefinitions = new ArrayList<>();
            if (definition.getMultiRouteDetails() == null) {
                return routeDefinitions;
            }
            definition.getMultiRouteDetails().forEach(multiRouteDetail ->
                    routeDefinitions.addAll(createRestRouteDefinition(service, definition.getContextPath(), multiRouteDetail)));
            return routeDefinitions;
        }


        private List<InboundRouteDefinition> createRestRouteDefinition(Service service, String contextPath, RestMultipleChannelServiceDefinition.MultiRouteDetail multiRouteDetail) {
            String serviceCode = service.getCode().trim();
            URIBuilder uri = createDefaultUri(gatewayChannel, serviceCode);
            RestChannelServiceDefinition definition = multiRouteDetail.getDefinition();
            if (definition != null) {
                if (definition.getMethod() != null) {
                    uri.setScheme("rest:" + definition.getMethod().getValue().toLowerCase());
                }
                applyRestPath(uri, gatewayChannel.getPath(), contextPath, definition.getPath());
            }

            RouteDefinition routeDefinition = routeBuilder.from(uri.toString())
                    .routeId(serviceCode + "-route-" + RouteUtils.getInstance().generateRouteUniqId(multiRouteDetail.getOperationCode()));
            routeDefinition.setProperty(Message.CHANNEL_SERVICE_DEFINITION, constant(definition));
            return Collections.singletonList(new InboundRouteDefinition(routeDefinition, definition));
        }


        private List<InboundRouteDefinition> createRestRouteDefinition(Service service, RestChannelServiceDefinition definition) {
            String serviceCode = service.getCode().trim();
            URIBuilder uri = createDefaultUri(gatewayChannel, serviceCode);
            if (definition != null) {
                if (definition.getMethod() != null) {
                    uri.setScheme("rest:" + definition.getMethod().getValue().toLowerCase());
                }
                applyRestPath(uri, gatewayChannel.getPath(), null, definition.getPath());
            }

            RouteDefinition routeDefinition = routeBuilder.from(uri.toString())
                    .routeId(serviceCode + "-route");
            routeDefinition.setProperty(Message.CHANNEL_SERVICE_DEFINITION, constant(definition));
            return Collections.singletonList(new InboundRouteDefinition(routeDefinition, definition));
        }

        private URIBuilder createDefaultUri(GatewayChannel gatewayChannel, String serviceCode) {
            log.debug("Creating REST route URI for service {}", serviceCode);
            return new URIBuilder()
                    .setScheme("rest:post")
                    .setPath(gatewayChannel.getPath())
                    .appendPath(serviceCode);
        }

        private void applyRestPath(URIBuilder uri, String gatewayPath, String contextPath, String routePath) {
            if (StringUtils.isAllBlank(contextPath, routePath)) {
                return;
            }
            uri.setPath(gatewayPath);
            appendPath(uri, contextPath);
            appendPath(uri, routePath);
        }

        private void appendPath(URIBuilder uri, String path) {
            String normalizedPath = StringUtils.trimToNull(path);
            if (normalizedPath != null) {
                normalizedPath = StringUtils.strip(normalizedPath, "/");
            }
            if (StringUtils.isNotBlank(normalizedPath)) {
                uri.appendPath(normalizedPath);
            }
        }
    }
}
