package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.common.service.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultRuntimeRoutePlanProviderTest {
    private final RuntimeTargetKindResolver kindResolver = mock(RuntimeTargetKindResolver.class);
    private final ChannelServiceAccessService accessService = mock(ChannelServiceAccessService.class);
    private final ChannelServiceDefinitionService definitionService = mock(ChannelServiceDefinitionService.class);
    private final ServiceOperationRepository operationRepository = mock(ServiceOperationRepository.class);
    private final ServiceOperationMapper operationMapper = mock(ServiceOperationMapper.class);
    private final DefaultRuntimeRoutePlanProvider provider = new DefaultRuntimeRoutePlanProvider(
            kindResolver,
            accessService,
            definitionService,
            operationRepository,
            operationMapper);

    @Test
    void channelPlanLoadsServicesFromChannelAccess() {
        GatewayChannel gatewayChannel = gateway("channel.mb");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition definition = definition(access);
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.CHANNEL);
        when(accessService.findAllByChannel(gatewayChannel.getChannel())).thenReturn(List.of(access));
        when(definitionService.findDefinitions(access, gatewayChannel)).thenReturn(List.of(definition));
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.CHANNEL, plan.targetKind());
        assertEquals(1, plan.servicePlans().size());
        assertEquals("card", plan.servicePlans().getFirst().service().getCode());
        assertEquals(List.of(definition), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanLoadsServicesFromGatewayDefinitions() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition definition = definition(access);
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(definition));
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.SERVICE_DOMAIN, plan.targetKind());
        assertEquals(1, plan.servicePlans().size());
        assertEquals(access.getId(), plan.servicePlans().getFirst().channelServiceAccess().getId());
    }

    private GatewayChannel gateway(String name) {
        Channel channel = new Channel();
        channel.setId((short) 1);
        channel.setCode("mb");

        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setId("gateway-1");
        gatewayChannel.setName(name);
        gatewayChannel.setActive(true);
        gatewayChannel.setChannel(channel);
        return gatewayChannel;
    }

    private ChannelServiceAccess activeAccess() {
        Channel channel = new Channel();
        channel.setId((short) 1);
        channel.setCode("mb");

        Service service = new Service();
        service.setId((short) 10);
        service.setCode("card");
        service.setPublish(true);
        service.setRoutingStrategy(RoutingStrategy.FIRST);

        ChannelServiceAccess access = new ChannelServiceAccess();
        access.setId(100L);
        access.setActive(true);
        access.setChannel(channel);
        access.setService(service);
        return access;
    }

    private ChannelServiceDefinition definition(ChannelServiceAccess access) {
        ChannelServiceDefinition definition = new ChannelServiceDefinition();
        definition.setId("definition-1");
        definition.setChannelServiceAccess(access);
        return definition;
    }
}
