package ir.daneshrefah.scm.core.config;

import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.utils.MessageInputContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.spi.Tracer;
import org.apache.camel.tracing.ActiveSpanManager;
import org.apache.camel.tracing.SpanAdapter;
import org.springframework.stereotype.Component;

@Component
public class CamelTracer implements Tracer {

    @Override

    public boolean shouldTrace(NamedNode definition) {
        return true;
    }

    @Override
    public void traceBeforeRoute(NamedRoute route, Exchange exchange) {
        SpanAdapter spanAdapter = ActiveSpanManager.getSpan(exchange);
        if (spanAdapter == null) {
            MessageInput currentContext = MessageInputContext.getCurrentContext();
            ActiveSpanManager.activate(exchange, currentContext.getSpanAdapter());
        }
    }

    @Override
    public void traceAfterRoute(NamedRoute route, Exchange exchange) {
        SpanAdapter spanAdapter = ActiveSpanManager.getSpan(exchange);
        if (spanAdapter == null) {
            MessageInput currentContext = MessageInputContext.getCurrentContext();
            ActiveSpanManager.activate(exchange, currentContext.getSpanAdapter());
        }
    }

    @Override
    public void traceBeforeNode(NamedNode node, Exchange exchange) {
    }

    @Override
    public void traceAfterNode(NamedNode node, Exchange exchange) {
    }

    @Override
    public void traceSentNode(NamedNode node, Exchange exchange, Endpoint endpoint, long elapsed) {
    }

    @Override
    public long getTraceCounter() {
        return 0;
    }

    @Override
    public void resetTraceCounter() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public boolean isStandby() {
        return true;
    }

    @Override
    public void setStandby(boolean standby) {
    }

    @Override
    public boolean isTraceRests() {
        return false;
    }

    @Override
    public void setTraceRests(boolean traceRests) {
    }

    @Override
    public boolean isTraceTemplates() {
        return false;
    }

    @Override
    public void setTraceTemplates(boolean traceTemplates) {
    }

    @Override
    public String getTracePattern() {
        return null;
    }

    @Override
    public void setTracePattern(String tracePattern) {
    }

    @Override
    public boolean isTraceBeforeAndAfterRoute() {
        return true;
    }

    @Override
    public void setTraceBeforeAndAfterRoute(boolean traceBeforeAndAfterRoute) {
    }

    @Override
    public ExchangeFormatter getExchangeFormatter() {
        return null;
    }

    @Override
    public void setExchangeFormatter(ExchangeFormatter exchangeFormatter) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
