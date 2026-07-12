package ir.daneshrefah.scm.core.integration.security;

import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.observation.starter.TraceContext;
import ir.daneshrefah.scm.observation.starter.TraceContextHolder;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.Processor;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.support.AsyncProcessorConverterHelper;
import org.apache.camel.support.AsyncProcessorSupport;
import org.springframework.stereotype.Component;

/**
 * Rebinds Exchange-owned authentication for only the synchronous portion of each Camel processor.
 * The binding is always restored before the worker thread returns to its pool.
 */
@Component
public class ExchangeSecurityContextInterceptStrategy implements InterceptStrategy {
    @Override
    public Processor wrapProcessorInInterceptors(
            CamelContext context,
            NamedNode definition,
            Processor target,
            Processor nextTarget
    ) {
        AsyncProcessor delegate = AsyncProcessorConverterHelper.convert(target);
        return new AsyncProcessorSupport() {
            @Override
            public boolean process(Exchange exchange, AsyncCallback callback) {
                TraceContextHolder.Scope traceBinding = bindTraceContext(exchange);
                try (ExchangeAuthenticationContext.Binding ignored = ExchangeAuthenticationContext.bind(exchange)) {
                    return delegate.process(exchange, callback);
                } finally {
                    if (traceBinding != null) {
                        traceBinding.close();
                    }
                }
            }

            @Override
            public String toString() {
                return "ExchangeSecurityContext[" + target + "]";
            }
        };
    }

    private TraceContextHolder.Scope bindTraceContext(Exchange exchange) {
        TraceContext context = activeTraceContext(exchange);
        return context == null ? null : TraceContextHolder.open(context);
    }

    private TraceContext activeTraceContext(Exchange exchange) {
        if (exchange == null) {
            return null;
        }
        TraceContext operation = exchange.getProperty(
                CoreObservationTraceSupport.OPERATION_CONTEXT_PROPERTY,
                TraceContext.class
        );
        if (operation != null) {
            return operation;
        }
        TraceContext service = exchange.getProperty(
                CoreObservationTraceSupport.SERVICE_CONTEXT_PROPERTY,
                TraceContext.class
        );
        if (service != null) {
            return service;
        }
        return exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_CONTEXT_PROPERTY,
                TraceContext.class
        );
    }
}
