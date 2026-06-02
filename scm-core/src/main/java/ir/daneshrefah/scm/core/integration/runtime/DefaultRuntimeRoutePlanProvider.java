package ir.daneshrefah.scm.core.integration.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.common.service.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultRuntimeRoutePlanProvider implements RuntimeRoutePlanProvider {
    private final RuntimeTargetKindResolver runtimeTargetKindResolver;
    private final ChannelServiceAccessService channelServiceAccessService;
    private final ChannelServiceDefinitionService channelServiceDefinitionService;
    private final ServiceOperationRepository serviceOperationRepository;
    private final ServiceOperationMapper serviceOperationMapper;
    private final ObjectMapper objectMapper;

    @Override
    public RuntimeRoutePlan provide(GatewayChannel gatewayChannel) {
        if (gatewayChannel == null) {
            throw new IllegalArgumentException("GatewayChannel is required for runtime route planning.");
        }
        if (!Boolean.TRUE.equals(gatewayChannel.getActive())) {
            throw new IllegalStateException("GatewayChannel '" + gatewayChannel.getName() + "' is inactive.");
        }

        RuntimeTargetKind targetKind = runtimeTargetKindResolver.resolve(gatewayChannel);
        List<RuntimeServicePlan> servicePlans = switch (targetKind) {
            case CHANNEL -> createChannelPlan(gatewayChannel);
            case SERVICE_DOMAIN -> createDomainPlan(gatewayChannel);
        };

        if (servicePlans.isEmpty()) {
            throw new IllegalStateException(
                    "No active service plan found for runtime target " + gatewayChannel.getName());
        }
        log.info("Runtime route plan created targetKind={} gatewayName={} serviceCount={}",
                targetKind, gatewayChannel.getName(), servicePlans.size());
        return new RuntimeRoutePlan(gatewayChannel, targetKind, servicePlans);
    }

    private List<RuntimeServicePlan> createChannelPlan(GatewayChannel gatewayChannel) {
        List<ChannelServiceAccess> accesses = Optional
                .ofNullable(channelServiceAccessService.findAllByChannel(gatewayChannel.getChannel()))
                .orElse(List.of());

        return accesses.stream()
                .filter(this::isActiveServiceAccess)
                .map(this::withServiceOperations)
                .filter(this::hasServiceOperations)
                .map(access -> new RuntimeServicePlan(
                        gatewayChannel,
                        access,
                        access.getService(),
                        channelServiceDefinitionService.findDefinitions(access, gatewayChannel)))
                .toList();
    }

    private List<RuntimeServicePlan> createDomainPlan(GatewayChannel gatewayChannel) {
        List<ChannelServiceDefinition> definitions = Optional
                .ofNullable(channelServiceDefinitionService.findDefinitions(gatewayChannel))
                .orElse(List.of());
        List<ChannelServiceDefinition> membershipDefinitions = definitions.stream()
                .filter(definition -> definition.getType() == ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER)
                .filter(definition -> definition.getChannelServiceAccess() != null)
                .filter(definition -> definition.getChannelServiceAccess().getId() != null)
                .peek(this::warnIgnoredMembershipContract)
                .toList();

        if (membershipDefinitions.isEmpty()) {
            throw new IllegalStateException("No SVC_DOMAIN_MEMBER definitions found for domain runtime "
                    + gatewayChannel.getName());
        }

        Map<String, List<ChannelServiceDefinition>> membershipsByService = membershipDefinitions.stream()
                .collect(Collectors.groupingBy(
                        definition -> serviceKey(definition.getChannelServiceAccess()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<String, List<ChannelServiceDefinition>> routeDefinitionsByService = definitions.stream()
                .filter(this::isInboundRouteDefinition)
                .filter(definition -> definition.getChannelServiceAccess() != null)
                .filter(definition -> definition.getChannelServiceAccess().getId() != null)
                .collect(Collectors.groupingBy(
                        definition -> serviceKey(definition.getChannelServiceAccess()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return membershipsByService.entrySet()
                .stream()
                .map(entry -> {
                    List<ChannelServiceAccess> memberAccesses = entry.getValue()
                            .stream()
                            .map(ChannelServiceDefinition::getChannelServiceAccess)
                            .map(this::withServiceOperations)
                            .filter(this::isActiveServiceAccess)
                            .filter(this::hasServiceOperations)
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toMap(
                                            ChannelServiceAccess::getId,
                                            access -> access,
                                            (first, second) -> first,
                                            LinkedHashMap::new),
                                    accessById -> List.copyOf(accessById.values())));
                    if (memberAccesses.isEmpty()) {
                        return null;
                    }
                    ChannelServiceAccess representativeAccess = memberAccesses.getFirst();
                    List<ChannelServiceDefinition> routeDefinitions = routeDefinitionsByService
                            .getOrDefault(entry.getKey(), List.of());
                    validateDomainExposure(gatewayChannel, representativeAccess, routeDefinitions);
                    return new RuntimeServicePlan(
                            gatewayChannel,
                            representativeAccess,
                            representativeAccess.getService(),
                            memberAccesses,
                            routeDefinitions);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isInboundRouteDefinition(ChannelServiceDefinition definition) {
        return definition.getType() == ChannelServiceDefinitionType.INBOUND_ROUTE
                || definition.getType() == ChannelServiceDefinitionType.INBOUND_ROUTE_GROUP;
    }

    private String serviceKey(ChannelServiceAccess access) {
        if (access == null || access.getService() == null) {
            throw new IllegalStateException("ChannelServiceAccess service is required for domain runtime planning.");
        }
        if (access.getService().getId() != null) {
            return "id:" + access.getService().getId();
        }
        if (access.getService().getCode() != null) {
            return "code:" + access.getService().getCode();
        }
        throw new IllegalStateException("Service id or code is required for domain runtime planning.");
    }

    private void validateDomainExposure(GatewayChannel gatewayChannel,
                                        ChannelServiceAccess access,
                                        List<ChannelServiceDefinition> routeDefinitions) {
        if (CollectionUtils.isNotEmpty(routeDefinitions)) {
            return;
        }
        Service service = access.getService();
        String serviceRef = service.getCode() != null
                ? "serviceCode=" + service.getCode()
                : "serviceId=" + service.getId();
        throw new IllegalStateException("Invalid domain runtime exposure gatewayName="
                + gatewayChannel.getName()
                + " "
                + serviceRef
                + ": missing INBOUND_ROUTE / INBOUND_ROUTE_GROUP. SVC_DOMAIN_MEMBER is membership only.");
    }

    private void warnIgnoredMembershipContract(ChannelServiceDefinition definition) {
        if (definition.getDefinition() == null || definition.getDefinition().getDetails() == null) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(definition.getDefinition().getDetails());
            if (root.hasNonNull("contract")) {
                log.warn("ClientContract under SVC_DOMAIN_MEMBER definition {} is ignored. "
                                + "Define contracts on INBOUND_ROUTE or INBOUND_ROUTE_GROUP.",
                        definition.getId());
            }
        } catch (Exception ignored) {
        }
    }

    private ChannelServiceAccess withServiceOperations(ChannelServiceAccess access) {
        if (access == null || access.getService() == null || access.getService().getId() == null) {
            return access;
        }
        List<ServiceOperationEntity> operationEntities = serviceOperationRepository
                .findAllByService_Id(access.getService().getId());
        List<ServiceOperation> serviceOperations = operationEntities.stream()
                .map(serviceOperationMapper::toModel)
                .toList();
        access.getService().setServiceOperations(serviceOperations);
        return access;
    }

    private boolean isActiveServiceAccess(ChannelServiceAccess access) {
        return access != null
                && Boolean.TRUE.equals(access.getActive())
                && access.getService() != null
                && Boolean.TRUE.equals(access.getService().getPublish())
                && Objects.nonNull(access.getService().getRoutingStrategy());
    }

    private boolean hasServiceOperations(ChannelServiceAccess access) {
        return access != null
                && access.getService() != null
                && CollectionUtils.isNotEmpty(access.getService().getServiceOperations());
    }
}
