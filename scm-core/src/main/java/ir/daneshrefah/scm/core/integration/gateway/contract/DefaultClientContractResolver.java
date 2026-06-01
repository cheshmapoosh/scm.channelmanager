package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultClientContractResolver implements ClientContractResolver {
    private static final ClientContract MODERN_REST_DEFAULT = new ClientContract(
            "modern-rest-v1",
            "jsonScmRequestDecoder",
            "jsonScmResponseEncoder",
            "restProblemDetailFaultEncoder");

    private final ObjectMapper objectMapper;

    @Override
    public ClientContract resolve(GatewayChannel gatewayChannel, ChannelServiceDefinition routeDefinition) {
        ClientContract routeContract = resolveRouteContract(routeDefinition);
        if (routeContract != null) {
            return routeContract;
        }
        if (gatewayChannel != null && gatewayChannel.getProtocolType() == ProtocolType.REST) {
            return MODERN_REST_DEFAULT;
        }
        throw new IllegalStateException("No default client contract configured for protocol "
                + (gatewayChannel != null ? gatewayChannel.getProtocolType() : null));
    }

    private ClientContract resolveRouteContract(ChannelServiceDefinition routeDefinition) {
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
            return new ClientContract(
                    required(contract, "name"),
                    required(contract, "requestDecoder"),
                    required(contract, "responseEncoder"),
                    required(contract, "faultEncoder"));
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
}
