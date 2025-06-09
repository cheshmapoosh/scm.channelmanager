package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.camel.model.rest.RestPropertyDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class RestProtocolHandler implements ProtocolHandler {
    @Override
    public ProtocolType getProtocol() {
        return ProtocolType.REST;
    }

    @Override
    public ProtocolConfigurer config(GatewayChannel gatewayChannel, RouteBuilder builder) {
        RestConfigurationDefinition restConfigurationDefinition = builder.restConfiguration()
                .component("servlet")
                .enableCORS(false);
        restConfigurationDefinition.setCorsHeaders(List.of(
                new RestPropertyDefinition("Access-Control-Allow-Origin", "*"),
                new RestPropertyDefinition("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"),
                new RestPropertyDefinition("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-Requested-With"),
                new RestPropertyDefinition("Access-Control-Allow-Credentials", "true"),
                new RestPropertyDefinition("Access-Control-Expose-Headers", "Custom-Header")
        ));
        String host = gatewayChannel.getHost();
        if (StringUtils.isNotEmpty(host)) {
            restConfigurationDefinition.host(host);
        }
        return new RestProtocolConfigurer(gatewayChannel, builder);
    }


    private record RestProtocolConfigurer(
            GatewayChannel gatewayChannel,
            RouteBuilder routeBuilder) implements ProtocolConfigurer {
        @Override
        public RouteDefinition routeDefinition(ChannelServiceAccess channelServiceAccess, List<ChannelServiceDefinition> channelServiceDefinitions) {
            Service service = channelServiceAccess.getService();
            String serviceCode = service.getCode().trim();
            URIBuilder uri = new URIBuilder()
                    .setScheme("rest:post")
                    .setPath(gatewayChannel.getPath())
                    .appendPath(serviceCode);
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

            return routeBuilder.from(uri.toString())
                    .routeId(serviceCode + "-route");
        }
    }
}
