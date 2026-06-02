package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultClientContractResolver implements ClientContractResolver {
    private final ObjectMapper objectMapper;
    private final ClientContractVersionResolver clientContractVersionResolver;

    @Override
    public ClientContract resolve(GatewayChannel gatewayChannel,
                                  ChannelServiceDefinition routeDefinition,
                                  String serviceVersion) {
        String resolvedVersion = clientContractVersionResolver.validate(serviceVersion);
        if (resolvedVersion == null) {
            resolvedVersion = clientContractVersionResolver.resolve(routeDefinition);
        }
        ClientContract routeContract = resolveRouteContract(routeDefinition, resolvedVersion);
        if (routeContract != null) {
            return routeContract;
        }
        if (gatewayChannel != null && gatewayChannel.getProtocolType() == ProtocolType.REST) {
            return defaultRestContract(resolvedVersion);
        }
        throw new IllegalStateException("No default client contract configured for protocol "
                + (gatewayChannel != null ? gatewayChannel.getProtocolType() : null));
    }

    private ClientContract resolveRouteContract(ChannelServiceDefinition routeDefinition, String serviceVersion) {
        if (routeDefinition == null
                || routeDefinition.getDefinition() == null
                || StringUtils.isBlank(routeDefinition.getDefinition().getDetails())) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(routeDefinition.getDefinition().getDetails());
            JsonNode contract = root.path("contract");
            if (contract.isMissingNode() || contract.isNull()) {
                return null;
            }
            if (routeDefinition.getType() == ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER) {
                log.warn("Ignoring ClientContract under SVC_DOMAIN_MEMBER definition {}. "
                        + "Contracts belong to INBOUND_ROUTE or INBOUND_ROUTE_GROUP definitions.",
                        routeDefinition.getId());
                return null;
            }
            return new ClientContract(
                    required(contract, "name"),
                    required(contract, "requestDecoder"),
                    required(contract, "responseEncoder"),
                    required(contract, "faultEncoder"),
                    serviceVersion);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid client contract details for route definition "
                    + routeDefinition.getId(), e);
        }
    }

    private String required(JsonNode node, String fieldName) {
        String value = StringUtils.trimToNull(node.path(fieldName).asText(null));
        if (value == null) {
            throw new IllegalArgumentException("Client contract field '" + fieldName + "' is required.");
        }
        return value;
    }

    private ClientContract defaultRestContract(String serviceVersion) {
        return new ClientContract(
                "rest-default",
                "jsonScmRequestDecoder",
                "jsonScmResponseEncoder",
                "restProblemDetailFaultEncoder",
                serviceVersion);
    }
}
