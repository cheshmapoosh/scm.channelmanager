package ir.daneshrefah.scm.core.integration.audit;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceAuditEventPublisherTest {
    private final List<AuditEvent> events = new ArrayList<>();
    private final ServiceAuditEventPublisher publisher = new ServiceAuditEventPublisher(
            events::add,
            new ScmExchangeMdc());

    @Test
    void recordsFinalServiceSuccess() {
        Exchange exchange = exchange();

        publisher.recordSuccess(exchange);

        assertEquals(1, events.size());
        AuditEvent event = events.getFirst();
        assertEquals("SERVICE", event.phase());
        assertEquals("SUCCESS", event.status());
        assertEquals("card", event.serviceCode());
        assertEquals("v2", event.serviceVersion());
        assertEquals("mb", event.channelCode());
    }

    @Test
    void recordsFinalServiceFailureFromScmFault() {
        Exchange exchange = exchange();
        exchange.getMessage().setBody(ScmFault.builder()
                .errors(List.of(new Error("service", 500, "service failed")))
                .build());

        publisher.recordFailure(exchange, new IllegalStateException("boom"));

        AuditEvent event = events.getFirst();
        assertEquals("FAILED", event.status());
        assertEquals("SCM-500", event.errorCode());
        assertEquals("service failed", event.errorMessage());
    }

    private Exchange exchange() {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        Service service = new Service();
        service.setCode("card");
        exchange.setProperty(Message.SERVICE, service);
        exchange.setProperty(Message.CHANNEL_CODE, "mb");
        exchange.setProperty(Message.SERVICE_VERSION, "v2");
        exchange.setProperty(Message.GATEWAY_NAME, "channel.mb");
        exchange.setProperty(Message.OPERATION_NAME, "CARD_INQUIRY");
        return exchange;
    }
}
