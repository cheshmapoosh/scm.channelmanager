package ir.daneshrefah.scm.core.integration.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            operationMapper,
            new ObjectMapper());

    @Test
    void channelServiceDefinitionTypeContainsOnlyFinalV9Values() {
        assertArrayEquals(
                new ChannelServiceDefinitionType[]{
                        ChannelServiceDefinitionType.INBOUND_ROUTE,
                        ChannelServiceDefinitionType.INBOUND_ROUTE_GROUP,
                        ChannelServiceDefinitionType.API_DOCUMENTATION,
                        ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER
                },
                ChannelServiceDefinitionType.values());
    }

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
    void channelPlanAllowsMissingExplicitRouteDefinitions() {
        GatewayChannel gatewayChannel = gateway("channel.mb");
        ChannelServiceAccess access = activeAccess();
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.CHANNEL);
        when(accessService.findAllByChannel(gatewayChannel.getChannel())).thenReturn(List.of(access));
        when(definitionService.findDefinitions(access, gatewayChannel)).thenReturn(List.of());
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.CHANNEL, plan.targetKind());
        assertEquals(1, plan.servicePlans().size());
        assertEquals(List.of(), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanLoadsServicesFromGatewayDefinitions() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition memberDefinition = definition(
                access,
                ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER,
                "member-1");
        ChannelServiceDefinition routeDefinition = definition(
                access,
                ChannelServiceDefinitionType.INBOUND_ROUTE,
                "route-1");
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(memberDefinition, routeDefinition));
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.SERVICE_DOMAIN, plan.targetKind());
        assertEquals(1, plan.servicePlans().size());
        assertEquals(access.getId(), plan.servicePlans().getFirst().channelServiceAccess().getId());
        assertEquals(List.of(routeDefinition), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanLoadsServiceWithInboundRouteGroupExposure() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition memberDefinition = definition(
                access,
                ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER,
                "member-1");
        ChannelServiceDefinition routeGroupDefinition = definition(
                access,
                ChannelServiceDefinitionType.INBOUND_ROUTE_GROUP,
                "route-group-1");
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(memberDefinition, routeGroupDefinition));
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(1, plan.servicePlans().size());
        assertEquals(List.of(routeGroupDefinition), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanFailsWhenGatewayDefinitionsDoNotIncludeMembership() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(
                definition(access, ChannelServiceDefinitionType.INBOUND_ROUTE, "route-1"),
                definition(access, ChannelServiceDefinitionType.API_DOCUMENTATION, "api-doc-1")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.provide(gatewayChannel));

        assertTrue(exception.getMessage().contains("No SVC_DOMAIN_MEMBER definitions"));
    }

    @Test
    void domainPlanFailsWhenMemberServiceHasNoInboundRouteExposure() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(
                definition(access, ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER, "member-1")));
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.provide(gatewayChannel));

        assertTrue(exception.getMessage().contains("gatewayName=domain.card"));
        assertTrue(exception.getMessage().contains("serviceCode=card"));
        assertTrue(exception.getMessage().contains("missing INBOUND_ROUTE / INBOUND_ROUTE_GROUP"));
        assertTrue(exception.getMessage().contains("SVC_DOMAIN_MEMBER is membership only"));
    }

    @Test
    void domainPlanFailsWhenMemberServiceOnlyHasApiDocumentation() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(
                definition(access, ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER, "member-1"),
                definition(access, ChannelServiceDefinitionType.API_DOCUMENTATION, "api-doc-1")));
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.provide(gatewayChannel));

        assertTrue(exception.getMessage().contains("missing INBOUND_ROUTE / INBOUND_ROUTE_GROUP"));
    }

    @Test
    void domainPlanCollapsesMembershipsByServiceAndKeepsRouteDefinitions() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess mobileAccess = activeAccess(100L, "mb", "card", (short) 10);
        ChannelServiceAccess internetAccess = activeAccess(101L, "ib", "card", (short) 10);
        ChannelServiceDefinition mobileMember = definition(
                mobileAccess,
                ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER,
                "member-mb");
        ChannelServiceDefinition internetMember = definition(
                internetAccess,
                ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER,
                "member-ib");
        ChannelServiceDefinition routeDefinition = definition(
                mobileAccess,
                ChannelServiceDefinitionType.INBOUND_ROUTE,
                "route-card");
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(
                mobileMember,
                internetMember,
                routeDefinition,
                definition(mobileAccess, ChannelServiceDefinitionType.API_DOCUMENTATION, "api-doc-1")));
        when(operationRepository.findAllByService_Id((short) 10)).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(1, plan.servicePlans().size());
        RuntimeServicePlan servicePlan = plan.servicePlans().getFirst();
        assertEquals("card", servicePlan.service().getCode());
        assertEquals(List.of(100L, 101L), servicePlan.channelServiceAccesses().stream()
                .map(ChannelServiceAccess::getId)
                .toList());
        assertEquals(List.of(routeDefinition), servicePlan.routeDefinitions());
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
        return activeAccess(100L, "mb", "card", (short) 10);
    }

    private ChannelServiceAccess activeAccess(Long id, String channelCode, String serviceCode, short serviceId) {
        Channel channel = new Channel();
        channel.setId((short) 1);
        channel.setCode(channelCode);

        Service service = new Service();
        service.setId(serviceId);
        service.setCode(serviceCode);
        service.setPublish(true);
        service.setRoutingStrategy(RoutingStrategy.FIRST);

        ChannelServiceAccess access = new ChannelServiceAccess();
        access.setId(id);
        access.setActive(true);
        access.setChannel(channel);
        access.setService(service);
        return access;
    }

    private ChannelServiceDefinition definition(ChannelServiceAccess access) {
        return definition(access, null, "definition-1");
    }

    private ChannelServiceDefinition definition(ChannelServiceAccess access,
                                                ChannelServiceDefinitionType type,
                                                String id) {
        ChannelServiceDefinition definition = new ChannelServiceDefinition();
        definition.setId(id);
        definition.setChannelServiceAccess(access);
        definition.setType(type);
        return definition;
    }
}
