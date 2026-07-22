package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.core.integration.service.routing.ChainOnApproveRoutingEngine;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingResultClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepObservationContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationRouteMetadataSetter;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.TraceContext;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationSink;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationSpec;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChainRoutingSpanPropagationTest {

    @Test
    void twoContinueStepsCreateSiblingOperationSpansUnderTheServiceSpan() {
        CapturingTraceSink traceSink = new CapturingTraceSink();
        CoreObservationTraceSupport traceSupport = traceSupport(traceSink);
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setBody("original-request");
        exchange.setProperty(
                CoreObservationTraceSupport.SERVICE_CONTEXT_PROPERTY,
                new TraceContext(
                        "shared-trace-id",
                        "service-parent-span-id",
                        "correlation-id",
                        "operation",
                        "01"
                )
        );
        RoutingStepPlan first = step(
                "approve-paymaster-process-for-execution",
                0,
                (currentExchange, context) -> context.originalRequest()
        );
        RoutingStepPlan second = step(
                "register-paymaster-in-nab",
                1,
                (currentExchange, context) ->
                        "nab-request:" + context.stepResults().get(first.stepName())
        );
        when(producerTemplate.send(eq(first.endpointUri()), eq(exchange)))
                .thenAnswer(invocation -> {
                    exchange.getMessage().setBody("approved-context");
                    return exchange;
                });
        when(producerTemplate.send(eq(second.endpointUri()), eq(exchange)))
                .thenAnswer(invocation -> {
                    assertEquals("nab-request:approved-context", exchange.getMessage().getBody());
                    exchange.getMessage().setBody("registered");
                    return exchange;
                });
        RoutingStepExecutor stepExecutor = new RoutingStepExecutor(
                new ServiceOperationRouteMetadataSetter(),
                producerTemplate,
                new RoutingResultClassifier(),
                traceSupport
        );

        RoutingExecutionResult result = new ChainOnApproveRoutingEngine(stepExecutor).execute(
                exchange,
                new RoutingPlan(
                        "my-paymaster:approve_and_execute",
                        RoutingStrategy.CHAIN_ON_APPROVE,
                        List.of(first, second)
                )
        );

        assertEquals(ChainStepDecision.CONTINUE, result.decision());
        assertEquals("registered", result.response());
        assertEquals(2, traceSink.started.size());
        assertEquals(2, traceSink.finishedCount);
        TraceObservationSpec firstSpan = traceSink.started.get(0);
        TraceObservationSpec secondSpan = traceSink.started.get(1);
        assertEquals("shared-trace-id", firstSpan.traceId());
        assertEquals(firstSpan.traceId(), secondSpan.traceId());
        assertEquals("service-parent-span-id", firstSpan.parentSpanId());
        assertEquals(firstSpan.parentSpanId(), secondSpan.parentSpanId());
        assertNotEquals(firstSpan.spanId(), secondSpan.spanId());
        assertEquals(first.stepName(), firstSpan.attributes()
                .get(CoreTraceAttributes.OPERATION_NAME.name()));
        assertEquals(second.stepName(), secondSpan.attributes()
                .get(CoreTraceAttributes.OPERATION_NAME.name()));
    }

    private RoutingStepPlan step(
            String operationName,
            int index,
            ir.daneshrefah.scm.core.integration.service.routing.RoutingStepRequestFactory requestFactory
    ) {
        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName(operationName);
        return new RoutingStepPlan(
                operationName,
                operation,
                "direct:op." + operationName,
                requestFactory,
                continuePolicy(),
                new RoutingStepObservationContext(
                        "my-paymaster",
                        "approve_and_execute",
                        index == 0 ? "APPROVE_PROCESS" : "BUSINESS_OPERATION",
                        index
                )
        );
    }

    private ChainStepDecisionPolicy continuePolicy() {
        return new ChainStepDecisionPolicy() {
            @Override
            public String code() {
                return "CONTINUE";
            }

            @Override
            public ChainStepDecision decide(ChainStepDecisionContext context) {
                return ChainStepDecision.CONTINUE;
            }
        };
    }

    private CoreObservationTraceSupport traceSupport(CapturingTraceSink traceSink) {
        ScmObservation observation = new ScmObservation(
                null,
                null,
                signal -> signal == ObservationSignal.TRACE,
                null,
                null,
                traceSink,
                (name, value) -> value,
                null,
                null,
                Clock.systemUTC()
        );
        @SuppressWarnings("unchecked")
        ObjectProvider<ScmObservation> observationProvider = mock(ObjectProvider.class);
        when(observationProvider.getIfAvailable()).thenReturn(observation);
        @SuppressWarnings("unchecked")
        ObjectProvider<GatewayAuthenticationTraceEnricher> enrichers = mock(ObjectProvider.class);
        return new CoreObservationTraceSupport(
                observationProvider,
                enrichers,
                new ScmExchangeMdc()
        );
    }

    private static final class CapturingTraceSink implements TraceObservationSink {
        private final List<TraceObservationSpec> started = new ArrayList<>();
        private int finishedCount;

        @Override
        public TraceObservationHandle start(TraceObservationSpec spec) {
            started.add(spec);
            return new TraceObservationHandle() {
                @Override
                public TraceContext traceContext() {
                    return new TraceContext(
                            spec.traceId(),
                            spec.spanId(),
                            spec.correlationId(),
                            spec.correlationType(),
                            spec.traceFlags()
                    );
                }

                @Override
                public void finish(
                        String outcome,
                        Map<String, Object> attributes,
                        Throwable throwable
                ) {
                    finishedCount++;
                }
            };
        }
    }
}
