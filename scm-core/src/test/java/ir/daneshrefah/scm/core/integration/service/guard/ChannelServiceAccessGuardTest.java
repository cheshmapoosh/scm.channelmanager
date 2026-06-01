package ir.daneshrefah.scm.core.integration.service.guard;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ChannelServiceAccessService;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelServiceAccessGuardTest {
    private final ChannelServiceAccessService accessService = mock(ChannelServiceAccessService.class);
    private final ChannelServiceAccessGuard guard = new ChannelServiceAccessGuard(
            accessService,
            new IncomingChannelCodeResolver());

    @Test
    void resolvesIncomingChannelAccessAndStoresItOnExchange() {
        Service service = service();
        ChannelServiceAccess mobileAccess = access(100L, "mb", service);
        ChannelServiceAccess internetAccess = access(101L, "IB", service);
        RuntimeServicePlan servicePlan = servicePlan(service, mobileAccess, internetAccess);
        Exchange exchange = exchange(service, "ib");

        when(accessService.findAllByServiceId(10L)).thenReturn(List.of(mobileAccess, internetAccess));

        guard.check(exchange, servicePlan);

        assertSame(internetAccess, exchange.getProperty(Message.CHANNEL_SERVICE_ACCESS));
    }

    @Test
    void rejectsRepositoryAccessOutsideRuntimeMembership() {
        Service service = service();
        ChannelServiceAccess mobileAccess = access(100L, "mb", service);
        ChannelServiceAccess internetAccess = access(101L, "ib", service);
        RuntimeServicePlan servicePlan = servicePlan(service, mobileAccess);
        Exchange exchange = exchange(service, "ib");

        when(accessService.findAllByServiceId(10L)).thenReturn(List.of(mobileAccess, internetAccess));

        assertThrows(AccessDeniedException.class, () -> guard.check(exchange, servicePlan));
    }

    private Exchange exchange(Service service, String channelCode) {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Message.SERVICE, service);
        exchange.setProperty(Message.CHANNEL_CODE, channelCode);
        return exchange;
    }

    private RuntimeServicePlan servicePlan(Service service, ChannelServiceAccess... accesses) {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setName("domain.card");
        return new RuntimeServicePlan(
                gatewayChannel,
                accesses[0],
                service,
                List.of(accesses),
                List.of());
    }

    private ChannelServiceAccess access(Long id, String channelCode, Service service) {
        Channel channel = new Channel();
        channel.setCode(channelCode);
        ChannelServiceAccess access = new ChannelServiceAccess();
        access.setId(id);
        access.setActive(true);
        access.setChannel(channel);
        access.setService(service);
        return access;
    }

    private Service service() {
        Service service = new Service();
        service.setId((short) 10);
        service.setCode("card");
        service.setPublish(true);
        service.setRoutingStrategy(RoutingStrategy.FIRST);
        return service;
    }
}
