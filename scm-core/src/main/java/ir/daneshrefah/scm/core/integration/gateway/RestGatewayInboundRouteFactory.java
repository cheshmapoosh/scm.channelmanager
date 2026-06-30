package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractVersionResolver;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestGatewayInboundRouteFactory implements GatewayInboundRouteFactory {
    private static final Pattern VERSION_PATH_PATTERN = Pattern.compile("^/?v[1-9][0-9]*(?:/.*)?$");

    private final ClientContractVersionResolver clientContractVersionResolver;

    @Override
    public ProtocolType protocol() {
        return ProtocolType.REST;
    }

    @Override
    public void configureGateway(GatewayInboundRouteFactoryContext context) {
        GatewayChannel gatewayChannel = context.gatewayChannel();
        RestConfigurationDefinition restConfigurationDefinition = context.routeBuilder().restConfiguration()
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
    }

    @Override
    public List<InboundRouteDefinition> createRoutes(GatewayInboundRouteContext context) {
        return createRestRouteDefinitions(
                context,
                clientContractVersionResolver);
    }

    private List<InboundRouteDefinition> createRestRouteDefinitions(
            GatewayInboundRouteContext context,
            ClientContractVersionResolver clientContractVersionResolver) {
        RuntimeServicePlan servicePlan = context.servicePlan();
        List<InboundRouteDefinition> routeDefinitions = new ArrayList<>();
        Service service = servicePlan.service();
        List<ChannelServiceDefinition> channelServiceDefinitions = servicePlan.routeDefinitions();
        if (channelServiceDefinitions == null || channelServiceDefinitions.isEmpty()) {
            return routeDefinitions;
        }
        Set<String> usedRouteIds = new HashSet<>();
        channelServiceDefinitions.forEach(channelServiceDefinition -> {
            if (channelServiceDefinition.getType() == null) {
                return;
            }
            routeDefinitions.addAll(
                    switch (channelServiceDefinition.getType()) {
                        case INBOUND -> createRestRouteDefinition(
                                context,
                                clientContractVersionResolver,
                                service,
                                (InboundChannelServiceDefinition) channelServiceDefinition,
                                usedRouteIds);
                        // API_DOC and SVC_DOMAIN_MEMBER are metadata only; they must never create inbound routes.
                        default -> Collections.emptyList();
                    }
            );
        });
        return routeDefinitions;
    }

    private List<InboundRouteDefinition> createRestRouteDefinition(GatewayInboundRouteContext context,
                                                                   ClientContractVersionResolver clientContractVersionResolver,
                                                                   Service service,
                                                                   InboundChannelServiceDefinition definition,
                                                                   Set<String> usedRouteIds) {
        GatewayChannel gatewayChannel = context.gatewayChannel();
        String serviceCode = service.getCode().trim();
        String serviceVersion = clientContractVersionResolver.resolve(definition);
        URIBuilder uri = createDefaultUri(gatewayChannel, serviceCode, serviceVersion);
        if (definition != null) {
            if (definition.getMethod() != null) {
                uri.setScheme("rest:" + definition.getMethod().getValue().toLowerCase());
            }
            applyRestPath(uri, gatewayChannel.getPath(), serviceVersion, definition.getPath());
        }

        RouteDefinition routeDefinition = context.routeBuilder().from(uri.toString())
                .routeId(uniqueRouteId(context, serviceCode, serviceVersion, definition, usedRouteIds));
        routeDefinition.setProperty(Message.SERVICE_VERSION, constant(serviceVersion));
        routeDefinition.setProperty(Message.CHANNEL_SERVICE_DEFINITION, constant(definition));
        return Collections.singletonList(new InboundRouteDefinition(routeDefinition, definition, serviceVersion));
    }

    private String uniqueRouteId(GatewayInboundRouteContext context,
                                 String serviceCode,
                                 String serviceVersion,
                                 InboundChannelServiceDefinition definition,
                                 Set<String> usedRouteIds) {
        GatewayChannel gatewayChannel = context.gatewayChannel();
        RuntimeTargetKind targetKind = context.routePlan().targetKind();
        String routeId = GatewayRouteIdFactory.singleRouteId(
                targetKind,
                gatewayChannel.getName(),
                serviceCode,
                serviceVersion);
        if (usedRouteIds.add(routeId)) {
            return routeId;
        }
        routeId = GatewayRouteIdFactory.inboundRouteId(
                targetKind,
                gatewayChannel.getName(),
                serviceCode,
                serviceVersion,
                definition);
        int sequence = 2;
        String candidate = routeId;
        while (!usedRouteIds.add(candidate)) {
            candidate = routeId + "-" + sequence++;
        }
        return candidate;
    }

    private URIBuilder createDefaultUri(GatewayChannel gatewayChannel, String serviceCode, String versionText) {
        log.debug("Creating REST route URI for service {}", serviceCode);
        return new URIBuilder()
                .setScheme("rest:post")
                .setPath(gatewayChannel.getPath())
                .appendPath(versionText)
                .appendPath(serviceCode);
    }

    private void applyRestPath(URIBuilder uri, String gatewayPath, String serviceVersion, String routePath) {
        if (StringUtils.isBlank(routePath)) {
            return;
        }
        uri.setPath(gatewayPath);
        if (!startsWithVersion(routePath)) {
            appendPath(uri, serviceVersion);
        }
        appendPath(uri, routePath);
    }

    private boolean startsWithVersion(String routePath) {
        String normalizedPath = StringUtils.trimToEmpty(routePath).toLowerCase(Locale.ROOT);
        return VERSION_PATH_PATTERN.matcher(normalizedPath).matches();
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
