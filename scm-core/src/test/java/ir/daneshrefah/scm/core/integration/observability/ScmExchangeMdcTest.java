package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.utils.constant.Constants;
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

    @Test
    void prefersStandardCorrelationIdHeader() {
        ScmExchangeMdc scmExchangeMdc = new ScmExchangeMdc();
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setHeader("X-Correlation-Id", "standard-correlation");
        exchange.getMessage().setHeader(Constants.SCM_PARAMETER_CORRELATION_ID, "legacy-correlation");

        try {
            assertEquals("standard-correlation", scmExchangeMdc.put(exchange).get("correlationId"));
            assertEquals("standard-correlation", exchange.getProperty(Message.CORRELATION_ID));
            assertEquals("standard-correlation", MDC.get("correlationId"));
        } finally {
            scmExchangeMdc.clear();
        }
    }
}
