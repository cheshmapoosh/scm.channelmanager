package ir.daneshrefah.scm.core.integration.service.guard;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuntimeChannelGuardTest {
    @Test
    void disabledGuardAllowsAnyChannel() {
        RuntimeChannelProperties properties = new RuntimeChannelProperties();
        properties.setEnabled(false);

        new RuntimeChannelGuard(properties, new IncomingChannelCodeResolver())
                .check(new DefaultExchange(new DefaultCamelContext()), servicePlan("ib"));
    }

    @Test
    void enabledGuardRejectsChannelOutsideAllowedList() {
        RuntimeChannelProperties properties = new RuntimeChannelProperties();
        properties.setEnabled(true);
        properties.setAllowedChannelCodes(List.of("mb"));

        RuntimeChannelGuard guard = new RuntimeChannelGuard(properties, new IncomingChannelCodeResolver());
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Message.CHANNEL_CODE, "ib");

        assertThrows(AccessDeniedException.class,
                () -> guard.check(exchange, servicePlan("mb")));
    }

    @Test
    void enabledGuardAllowsLowercaseIncomingWhenConfiguredUppercase() {
        RuntimeChannelProperties properties = new RuntimeChannelProperties();
        properties.setEnabled(true);
        properties.setAllowedChannelCodes(List.of("MB"));

        RuntimeChannelGuard guard = new RuntimeChannelGuard(properties, new IncomingChannelCodeResolver());
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Message.CHANNEL_CODE, "mb");

        assertDoesNotThrow(() -> guard.check(exchange, servicePlan("mb")));
    }

    @Test
    void enabledGuardAllowsUppercaseIncomingWhenConfiguredLowercase() {
        RuntimeChannelProperties properties = new RuntimeChannelProperties();
        properties.setEnabled(true);
        properties.setAllowedChannelCodes(List.of("mb"));

        RuntimeChannelGuard guard = new RuntimeChannelGuard(properties, new IncomingChannelCodeResolver());
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Message.CHANNEL_CODE, "MB");

        assertDoesNotThrow(() -> guard.check(exchange, servicePlan("mb")));
    }

    private RuntimeServicePlan servicePlan(String channelCode) {
        Channel channel = new Channel();
        channel.setCode(channelCode);
        ChannelServiceAccess access = new ChannelServiceAccess();
        access.setChannel(channel);
        access.setService(new Service());
        return new RuntimeServicePlan(null, access, access.getService(), List.of());
    }
}
