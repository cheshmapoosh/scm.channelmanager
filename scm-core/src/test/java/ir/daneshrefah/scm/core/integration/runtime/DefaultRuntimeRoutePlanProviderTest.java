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
                        ChannelServiceDefinitionType.INBOUND,
                        ChannelServiceDefinitionType.API_DOC,
                        ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER
                },
                ChannelServiceDefinitionType.values());
    }

    @Test
    void channelPlanRequiresInboundAndAllowsOptionalApiDocDefinition() {
        GatewayChannel gatewayChannel = gateway("channel.mb");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition inbound = definition(access, ChannelServiceDefinitionType.INBOUND, "route-1");
        ChannelServiceDefinition apiDoc = definition(access, ChannelServiceDefinitionType.API_DOC, "api-doc-1");
        arrangeChannelAccess(gatewayChannel, access, List.of(inbound, apiDoc));

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.CHANNEL, plan.targetKind());
        assertEquals(1, plan.servicePlans().size());
        assertEquals("card", plan.servicePlans().getFirst().service().getCode());
        assertEquals(List.of(inbound), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void channelPlanFailsWhenInboundDefinitionIsMissing() {
        GatewayChannel gatewayChannel = gateway("channel.mb");
        ChannelServiceAccess access = activeAccess();
        arrangeChannelAccess(gatewayChannel, access, List.of(
                definition(access, ChannelServiceDefinitionType.API_DOC, "api-doc-1")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.provide(gatewayChannel));

        assertInvalidDefinitionMessage(exception, "channel.mb", RuntimeTargetKind.CHANNEL, "card", "INBOUND");
    }

    @Test
    void channelPlanDoesNotFailWhenApiDocDefinitionIsMissing() {
        GatewayChannel gatewayChannel = gateway("channel.mb");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition inbound = definition(access, ChannelServiceDefinitionType.INBOUND, "route-1");
        arrangeChannelAccess(gatewayChannel, access, List.of(
                inbound));

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.CHANNEL, plan.targetKind());
        assertEquals(List.of(inbound), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanRequiresMembershipInboundAndAllowsOptionalApiDocDefinition() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition member = definition(access, ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER, "member-1");
        ChannelServiceDefinition inbound = definition(access, ChannelServiceDefinitionType.INBOUND, "route-1");
        ChannelServiceDefinition apiDoc = definition(access, ChannelServiceDefinitionType.API_DOC, "api-doc-1");
        arrangeDomainDefinitions(gatewayChannel, access, List.of(member, inbound, apiDoc));

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.SERVICE_DOMAIN, plan.targetKind());
        assertEquals(1, plan.servicePlans().size());
        assertEquals(access.getId(), plan.servicePlans().getFirst().channelServiceAccess().getId());
        assertEquals(List.of(inbound), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanFailsWhenGatewayDefinitionsDoNotIncludeMembership() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(
                definition(access, ChannelServiceDefinitionType.INBOUND, "route-1"),
                definition(access, ChannelServiceDefinitionType.API_DOC, "api-doc-1")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.provide(gatewayChannel));

        assertTrue(exception.getMessage().contains("gatewayName=domain.card"));
        assertTrue(exception.getMessage().contains("targetKind=SERVICE_DOMAIN"));
        assertTrue(exception.getMessage().contains("missing SVC_DOMAIN_MEMBER definition"));
    }

    @Test
    void domainPlanFailsWhenMemberServiceHasNoInboundDefinition() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        arrangeDomainDefinitions(gatewayChannel, access, List.of(
                definition(access, ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER, "member-1"),
                definition(access, ChannelServiceDefinitionType.API_DOC, "api-doc-1")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.provide(gatewayChannel));

        assertInvalidDefinitionMessage(exception, "domain.card", RuntimeTargetKind.SERVICE_DOMAIN, "card", "INBOUND");
    }

    @Test
    void domainPlanDoesNotFailWhenMemberServiceHasNoApiDocDefinition() {
        GatewayChannel gatewayChannel = gateway("domain.card");
        ChannelServiceAccess access = activeAccess();
        ChannelServiceDefinition inbound = definition(access, ChannelServiceDefinitionType.INBOUND, "route-1");
        arrangeDomainDefinitions(gatewayChannel, access, List.of(
                definition(access, ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER, "member-1"),
                inbound));

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(RuntimeTargetKind.SERVICE_DOMAIN, plan.targetKind());
        assertEquals(List.of(inbound), plan.servicePlans().getFirst().routeDefinitions());
    }

    @Test
    void domainPlanCollapsesMembershipsByServiceAndKeepsOnlyInboundRouteDefinitions() {
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
        ChannelServiceDefinition inbound = definition(
                mobileAccess,
                ChannelServiceDefinitionType.INBOUND,
                "route-card");
        ChannelServiceDefinition apiDoc = definition(
                mobileAccess,
                ChannelServiceDefinitionType.API_DOC,
                "api-doc-card");

        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(List.of(
                mobileMember,
                internetMember,
                inbound,
                apiDoc));
        arrangeOperations(mobileAccess);
        arrangeOperations(internetAccess);

        RuntimeRoutePlan plan = provider.provide(gatewayChannel);

        assertEquals(1, plan.servicePlans().size());
        RuntimeServicePlan servicePlan = plan.servicePlans().getFirst();
        assertEquals("card", servicePlan.service().getCode());
        assertEquals(List.of(100L, 101L), servicePlan.channelServiceAccesses().stream()
                .map(ChannelServiceAccess::getId)
                .toList());
        assertEquals(List.of(inbound), servicePlan.routeDefinitions());
    }

    private void arrangeChannelAccess(GatewayChannel gatewayChannel,
                                      ChannelServiceAccess access,
                                      List<ChannelServiceDefinition> definitions) {
        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.CHANNEL);
        when(accessService.findAllByChannel(gatewayChannel.getChannel())).thenReturn(List.of(access));
        when(definitionService.findDefinitions(access, gatewayChannel)).thenReturn(definitions);
        arrangeOperations(access);
    }

    private void arrangeDomainDefinitions(GatewayChannel gatewayChannel,
                                          ChannelServiceAccess access,
                                          List<ChannelServiceDefinition> definitions) {
        when(kindResolver.resolve(gatewayChannel)).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(definitionService.findDefinitions(gatewayChannel)).thenReturn(definitions);
        arrangeOperations(access);
    }

    private void arrangeOperations(ChannelServiceAccess access) {
        ServiceOperationEntity operationEntity = new ServiceOperationEntity();
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName("CARD_INQUIRY");
        when(operationRepository.findAllByService_Id(access.getService().getId())).thenReturn(List.of(operationEntity));
        when(operationMapper.toModel(operationEntity)).thenReturn(serviceOperation);
    }

    private void assertInvalidDefinitionMessage(IllegalStateException exception,
                                                String gatewayName,
                                                RuntimeTargetKind targetKind,
                                                String serviceCode,
                                                String missingType) {
        assertTrue(exception.getMessage().contains("gatewayName=" + gatewayName));
        assertTrue(exception.getMessage().contains("targetKind=" + targetKind));
        assertTrue(exception.getMessage().contains("serviceCode=" + serviceCode));
        assertTrue(exception.getMessage().contains("missing " + missingType + " definition"));
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
