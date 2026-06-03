package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScmExchangeMdcTest {
    @Test
    void clearRemovesScmMdcFields() {
        ScmExchangeMdc scmExchangeMdc = new ScmExchangeMdc();
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Message.CHANNEL_CODE, "mb");
        exchange.setProperty(Message.SERVICE_VERSION, "v2");

        try {
            scmExchangeMdc.put(exchange);
            assertEquals("mb", MDC.get("channelCode"));
            assertEquals("v2", MDC.get("serviceVersion"));

            scmExchangeMdc.clear();

            assertNull(MDC.get("channelCode"));
            assertNull(MDC.get("serviceVersion"));
        } finally {
            scmExchangeMdc.clear();
        }
    }
}
