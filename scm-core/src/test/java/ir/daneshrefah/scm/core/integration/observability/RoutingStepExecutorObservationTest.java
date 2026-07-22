package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.core.integration.service.routing.ChainOnApproveRoutingEngine;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.FirstRoutingEngine;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingResultClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingRetryLaterException;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepObservationContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationRouteMetadataSetter;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.TraceContext;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationSink;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationSpec;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoutingStepExecutorObservationTest {

    @Test
    void executedStepOwnsOneOperationSpanWithRoutingAttributes() {
        CapturingTraceSink traceSink = new CapturingTraceSink();
        CoreObservationTraceSupport traceSupport = traceSupport(traceSink);
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        Exchange exchange = exchangeWithServiceParent();
        RoutingStepPlan step = step(
                "register-paymaster-in-nab",
                normalizedPolicy(ChainStepDecision.CONTINUE, "SUCCESS_ALREADY_APPLIED"),
                1,
                "client"
        );
        Operation operation = operation("register-paymaster-in-nab", OperationType.REST);
        when(producerTemplate.send(eq(step.endpointUri()), eq(exchange))).thenAnswer(invocation -> {
            traceSupport.startOperationCall(exchange, operation);
            exchange.getMessage().setBody("registered");
            traceSupport.finishOperationCallSuccess(exchange, operation);
            return exchange;
        });

        RoutingExecutionContext executionContext = new RoutingExecutionContext("request");
        RoutingStepExecutionResult result = executor(producerTemplate, traceSupport).execute(
                exchange,
                RoutingStrategy.CHAIN_ON_APPROVE,
                step,
                executionContext
        );

        assertEquals("registered", result.response());
        assertEquals(ChainStepDecision.CONTINUE, result.decision());
        assertEquals("registered", executionContext.stepResults().get("register-paymaster-in-nab"));
        assertEquals(1, traceSink.started.size());
        assertEquals(1, traceSink.finished.size());

        TraceObservationSpec span = traceSink.started.getFirst();
        assertEquals("operation.call", span.spanName());
        assertEquals("client", span.spanKind());
        assertEquals("trace-id", span.traceId());
        assertEquals("service-span-id", span.parentSpanId());
        assertNotEquals(span.parentSpanId(), span.spanId());
        assertEquals("paymaster", span.attributes().get(CoreTraceAttributes.SERVICE_CODE.name()));
        assertEquals("register-paymaster-in-nab",
                span.attributes().get(CoreTraceAttributes.OPERATION_NAME.name()));
        assertEquals("CHAIN_ON_APPROVE",
                span.attributes().get(CoreTraceAttributes.ROUTING_STRATEGY.name()));
        assertEquals(1L, span.attributes().get(CoreTraceAttributes.ROUTING_STEP_INDEX.name()));
        assertEquals("approve_and_execute",
                span.attributes().get(CoreTraceAttributes.TASK_INBOUND_ACTION.name()));
        assertEquals("BUSINESS_OPERATION",
                span.attributes().get(CoreTraceAttributes.TASK_ROLE.name()));
        assertEquals("success", traceSink.finished.getFirst().outcome());
        assertEquals("CONTINUE", traceSink.finished.getFirst().attributes()
                .get(CoreTraceAttributes.CHAIN_DECISION.name()));
        assertEquals("REST", traceSink.finished.getFirst().attributes()
                .get(CoreTraceAttributes.OPERATION_TYPE.name()));
        assertEquals("SUCCESS_ALREADY_APPLIED", traceSink.finished.getFirst().attributes()
                .get(CoreTraceAttributes.OPERATION_NORMALIZED_OUTCOME.name()));
        assertTrue(traceSink.finished.getFirst().attributes().keySet().stream()
                .noneMatch(name -> name.toLowerCase().contains("payload")));
    }

    @Test
    void firstCreatesExactlyOneSpanAndAcceptsANullPojoResponse() {
        CapturingTraceSink traceSink = new CapturingTraceSink();
        CoreObservationTraceSupport traceSupport = traceSupport(traceSink);
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        Exchange exchange = exchangeWithServiceParent();
        RoutingStepPlan step = step("start-paymaster-process", (ChainStepDecisionPolicy) null, 0);
        when(producerTemplate.send(eq(step.endpointUri()), eq(exchange))).thenAnswer(invocation -> {
            exchange.getMessage().setBody(null);
            return exchange;
        });
        FirstRoutingEngine engine = new FirstRoutingEngine(executor(producerTemplate, traceSupport));

        var result = engine.execute(
                exchange,
                new RoutingPlan("start", RoutingStrategy.FIRST, List.of(step))
        );

        assertEquals(ChainStepDecision.CONTINUE, result.decision());
        assertTrue(result.context().stepResults().containsKey("start-paymaster-process"));
        assertEquals(1, traceSink.started.size());
        assertEquals(1, traceSink.finished.size());
        assertEquals("FIRST", traceSink.started.getFirst().attributes()
                .get(CoreTraceAttributes.ROUTING_STRATEGY.name()));
        assertEquals("success", traceSink.finished.getFirst().outcome());
    }

    @Test
    void retryLaterStopsBeforeSkippedStepCreatesASpan() {
        CapturingTraceSink traceSink = new CapturingTraceSink();
        CoreObservationTraceSupport traceSupport = traceSupport(traceSink);
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        Exchange exchange = exchangeWithServiceParent();
        RoutingStepPlan retry = step("register-paymaster-in-nab", ChainStepDecision.RETRY_LATER, 0);
        RoutingStepPlan skipped = step("grant-paymaster-account-access-in-scm", ChainStepDecision.CONTINUE, 1);
        when(producerTemplate.send(any(String.class), eq(exchange))).thenAnswer(invocation -> {
            exchange.getMessage().setBody("unknown");
            return exchange;
        });
        ChainOnApproveRoutingEngine engine = new ChainOnApproveRoutingEngine(
                executor(producerTemplate, traceSupport));

        assertThrows(RoutingRetryLaterException.class, () -> engine.execute(
                exchange,
                new RoutingPlan("send", RoutingStrategy.CHAIN_ON_APPROVE, List.of(retry, skipped))
        ));

        assertEquals(1, traceSink.started.size());
        assertEquals(1, traceSink.finished.size());
        assertEquals("unknown", traceSink.finished.getFirst().outcome());
        assertEquals("RETRY_LATER", traceSink.finished.getFirst().attributes()
                .get(CoreTraceAttributes.CHAIN_DECISION.name()));
    }

    @Test
    void sendFailureIsPreservedAndClosesTheSpanOnce() {
        CapturingTraceSink traceSink = new CapturingTraceSink();
        CoreObservationTraceSupport traceSupport = traceSupport(traceSink);
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        Exchange exchange = exchangeWithServiceParent();
        RoutingStepPlan step = step("failed-operation", ChainStepDecision.FAIL, 0);
        IllegalStateException failure = new IllegalStateException("route failed");
        when(producerTemplate.send(eq(step.endpointUri()), eq(exchange))).thenThrow(failure);

        RoutingStepExecutionResult result = executor(producerTemplate, traceSupport).execute(
                exchange,
                RoutingStrategy.CHAIN_ON_APPROVE,
                step,
                new RoutingExecutionContext("request")
        );

        assertSame(failure, result.failure());
        assertSame(failure, exchange.getProperty(Exchange.EXCEPTION_CAUGHT));
        assertEquals(1, traceSink.started.size());
        assertEquals(1, traceSink.finished.size());
        assertEquals("failure", traceSink.finished.getFirst().outcome());
        assertSame(failure, traceSink.finished.getFirst().failure());
    }

    private RoutingStepExecutor executor(
            ProducerTemplate producerTemplate,
            CoreObservationTraceSupport traceSupport
    ) {
        return new RoutingStepExecutor(
                new ServiceOperationRouteMetadataSetter(),
                producerTemplate,
                new RoutingResultClassifier(),
                traceSupport
        );
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

    private Exchange exchangeWithServiceParent() {
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setBody("request");
        exchange.setProperty(
                CoreObservationTraceSupport.SERVICE_CONTEXT_PROPERTY,
                new TraceContext("trace-id", "service-span-id", "correlation-id", "operation", "01")
        );
        return exchange;
    }

    private RoutingStepPlan step(String operationName, ChainStepDecision decision, int index) {
        return step(operationName, policy(decision), index);
    }

    private RoutingStepPlan step(
            String operationName,
            ChainStepDecisionPolicy policy,
            int index
    ) {
        return step(operationName, policy, index, "internal");
    }

    private RoutingStepPlan step(
            String operationName,
            ChainStepDecisionPolicy policy,
            int index,
            String spanKind
    ) {
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName(operationName);
        return new RoutingStepPlan(
                operationName,
                serviceOperation,
                "direct:op." + operationName,
                (exchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext(
                        "paymaster",
                        "approve_and_execute",
                        "BUSINESS_OPERATION",
                        index,
                        spanKind
                )
        );
    }

    private ChainStepDecisionPolicy normalizedPolicy(
            ChainStepDecision decision,
            String normalizedOutcome
    ) {
        return new ChainStepDecisionPolicy() {
            @Override
            public String code() {
                return decision.name();
            }

            @Override
            public ChainStepDecision decide(ChainStepDecisionContext context) {
                context.exchange().setProperty(
                        RoutingStepExecutor.NORMALIZED_OUTCOME_PROPERTY,
                        normalizedOutcome
                );
                return decision;
            }
        };
    }

    private ChainStepDecisionPolicy policy(ChainStepDecision decision) {
        return new ChainStepDecisionPolicy() {
            @Override
            public String code() {
                return decision.name();
            }

            @Override
            public ChainStepDecision decide(ChainStepDecisionContext context) {
                return decision;
            }
        };
    }

    private Operation operation(String name, OperationType type) {
        Operation operation = new Operation();
        operation.setName(name);
        operation.setType(type);
        return operation;
    }

    private static final class CapturingTraceSink implements TraceObservationSink {
        private final List<TraceObservationSpec> started = new ArrayList<>();
        private final List<FinishedSpan> finished = new ArrayList<>();

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
                    finished.add(new FinishedSpan(
                            outcome,
                            new LinkedHashMap<>(attributes),
                            throwable
                    ));
                }
            };
        }
    }

    private record FinishedSpan(
            String outcome,
            Map<String, Object> attributes,
            Throwable failure
    ) {
    }
}
