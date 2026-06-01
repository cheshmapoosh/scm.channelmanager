package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
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
        List<ChannelServiceDefinition> definitions = channelServiceDefinitionService.findDefinitions(gatewayChannel);
        Map<Long, List<ChannelServiceDefinition>> definitionsByAccessId = definitions.stream()
                .filter(definition -> definition.getChannelServiceAccess() != null)
                .filter(definition -> definition.getChannelServiceAccess().getId() != null)
                .collect(Collectors.groupingBy(
                        definition -> definition.getChannelServiceAccess().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return definitionsByAccessId.values()
                .stream()
                .map(groupedDefinitions -> {
                    ChannelServiceAccess access = withServiceOperations(groupedDefinitions.getFirst().getChannelServiceAccess());
                    return new RuntimeServicePlan(gatewayChannel, access, access.getService(), groupedDefinitions);
                })
                .filter(plan -> isActiveServiceAccess(plan.channelServiceAccess()))
                .filter(plan -> hasServiceOperations(plan.channelServiceAccess()))
                .toList();
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
