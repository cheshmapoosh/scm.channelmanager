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

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultClientContractResolver implements ClientContractResolver {
    public static final String SOURCE_ROUTE_DEFINITION = "route-definition";
    public static final String SOURCE_PROTOCOL_VERSION_DEFAULT = "protocol-version-default";
    public static final String SOURCE_PROTOCOL_DEFAULT = "protocol-default";

    private final ObjectMapper objectMapper;
    private final ClientContractVersionResolver clientContractVersionResolver;
    private final ScmGatewayClientContractProperties clientContractProperties;

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
        ProtocolType protocol = gatewayChannel != null ? gatewayChannel.getProtocolType() : null;
        return resolveDefaultContract(protocol, resolvedVersion);
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
            if (routeDefinition.getType() == ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER
                    || routeDefinition.getType() == ChannelServiceDefinitionType.API_DOC) {
                log.warn("Ignoring ClientContract under {} definition {}. Contracts belong to INBOUND definitions.",
                        routeDefinition.getType(), routeDefinition.getId());
                return null;
            }
            return new ClientContract(
                    required(contract, "name"),
                    required(contract, "requestDecoder"),
                    required(contract, "responseEncoder"),
                    required(contract, "faultEncoder"),
                    serviceVersion,
                    SOURCE_ROUTE_DEFINITION);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid client contract details for route definition "
                    + routeDefinition.getId(), e);
        }
    }

    private ClientContract resolveDefaultContract(ProtocolType protocol, String serviceVersion) {
        if (protocol == null) {
            throw noDefaultContract(protocol, serviceVersion);
        }
        ScmGatewayClientContractProperties.ProtocolDefaults protocolDefaults =
                clientContractProperties.getDefaults().get(protocol);
        if (protocolDefaults == null) {
            throw noDefaultContract(protocol, serviceVersion);
        }

        ScmGatewayClientContractProperties.ContractDefinition versionDefault =
                versionContract(protocolDefaults.getVersions(), serviceVersion);
        if (versionDefault != null) {
            return toClientContract(versionDefault, serviceVersion, SOURCE_PROTOCOL_VERSION_DEFAULT, protocol);
        }

        ScmGatewayClientContractProperties.ContractDefinition protocolDefault = protocolDefaults.getDefault();
        if (protocolDefault != null) {
            return toClientContract(protocolDefault, serviceVersion, SOURCE_PROTOCOL_DEFAULT, protocol);
        }
        throw noDefaultContract(protocol, serviceVersion);
    }

    private ScmGatewayClientContractProperties.ContractDefinition versionContract(
            Map<String, ScmGatewayClientContractProperties.ContractDefinition> versions,
            String serviceVersion
    ) {
        String version = StringUtils.trimToNull(serviceVersion);
        if (version == null || versions == null || versions.isEmpty()) {
            return null;
        }
        ScmGatewayClientContractProperties.ContractDefinition exact = versions.get(version);
        if (exact != null) {
            return exact;
        }
        String normalized = version.toLowerCase(Locale.ROOT);
        return versions.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getKey().toLowerCase(Locale.ROOT).equals(normalized))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private ClientContract toClientContract(ScmGatewayClientContractProperties.ContractDefinition definition,
                                            String serviceVersion,
                                            String source,
                                            ProtocolType protocol) {
        if (definition == null) {
            throw noDefaultContract(protocol, serviceVersion);
        }
        return new ClientContract(
                required(definition.getName(), "name", protocol, serviceVersion, source),
                required(definition.getRequestDecoder(), "request-decoder", protocol, serviceVersion, source),
                required(definition.getResponseEncoder(), "response-encoder", protocol, serviceVersion, source),
                required(definition.getFaultEncoder(), "fault-encoder", protocol, serviceVersion, source),
                serviceVersion,
                source);
    }

    private String required(String value,
                            String fieldName,
                            ProtocolType protocol,
                            String serviceVersion,
                            String source) {
        String cleaned = StringUtils.trimToNull(value);
        if (cleaned == null) {
            throw new IllegalStateException("Default client contract field '" + fieldName
                    + "' is required for protocol " + protocol
                    + " and serviceVersion " + serviceVersion
                    + " from " + source);
        }
        return cleaned;
    }

    private IllegalStateException noDefaultContract(ProtocolType protocol, String serviceVersion) {
        return new IllegalStateException("No default client contract configured for protocol " + protocol
                + " and serviceVersion " + serviceVersion
                + ". Configure scm.gateway.client-contracts.defaults." + protocol
                + ".default or scm.gateway.client-contracts.defaults." + protocol
                + ".versions." + serviceVersion);
    }

    private String required(JsonNode node, String fieldName) {
        String value = StringUtils.trimToNull(node.path(fieldName).asText(null));
        if (value == null) {
            throw new IllegalArgumentException("Client contract field '" + fieldName + "' is required.");
        }
        return value;
    }
}
