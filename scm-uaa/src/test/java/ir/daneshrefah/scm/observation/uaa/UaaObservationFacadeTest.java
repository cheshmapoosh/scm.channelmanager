package ir.daneshrefah.scm.observation.uaa;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ir.daneshrefah.scm.observation.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.ObservationEvent;
import ir.daneshrefah.scm.observation.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.ObservationEventSignal;
import ir.daneshrefah.scm.observation.ObservationEventSink;
import ir.daneshrefah.scm.observation.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.SecretScrubbingObservationSanitizer;
import ir.daneshrefah.scm.observation.TraceContextHolder;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.trace.StructuredTraceObservationSink;
import ir.daneshrefah.scm.uaa.observation.UaaObservation;
import ir.daneshrefah.scm.uaa.observation.UaaObservationAttributeContributor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaObservationFacadeTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clearContext() {
        TraceContextHolder.clear();
    }

    @Test
    void controllerRootSpanHasNoParentSpan() {
        List<ObservationEvent> events = new ArrayList<>();
        UaaObservation observation = new UaaObservation(observation(events));

        UaaObservation.ControllerContext ctx = controllerContext();
        try (ObservationScope scope = observation.traceController(ctx)) {
            scope.success();
        }

        Map<String, Object> document = document(events, "uaa.controller.login.token");
        assertFalse(document.containsKey("parent.span.id"));
        assertEquals("server", document.get("span.kind"));
        assertEquals("request", document.get("correlation.type"));
    }

    @Test
    void serviceChildSpanUsesCurrentTraceAndParentSpan() {
        List<ObservationEvent> events = new ArrayList<>();
        UaaObservation observation = new UaaObservation(observation(events));

        ObservationScope controller = observation.traceController(controllerContext());
        ObservationScope child = observation.traceActivationPwa(new UaaObservation.OperationContext(
                "uaa.activation.pwa.request", "uaa.activation.pwa", "request", "started", null));
        child.success().close();
        controller.success().close();

        Map<String, Object> rootDocument = document(events, "uaa.controller.login.token");
        Map<String, Object> childDocument = document(events, "uaa.activation.pwa.request");
        assertEquals(rootDocument.get("trace.id"), childDocument.get("trace.id"));
        assertEquals(rootDocument.get("span.id"), childDocument.get("parent.span.id"));
    }

    @Test
    void jwtMaskedIsFivePlusEllipsisPlusFiveAndSensitiveFailuresAreScrubbed() {
        List<ObservationEvent> events = new ArrayList<>();
        UaaObservation observation = new UaaObservation(observation(events));

        assertEquals("abcde...vwxyz", observation.jwtMasked("abcdefghijklmnopqrstuvwxyz"));

        UaaObservation.JwtContext ctx = new UaaObservation.JwtContext(
                true,
                "bearer",
                "issuer",
                "subject",
                "username",
                observation.jwtMasked("abcdefghijklmnopqrstuvwxyz"),
                null
        );
        try (ObservationScope scope = observation.traceJwtValidation(ctx)) {
            scope.failure(new RuntimeException("password=secret otp=123456 access_token=rawtoken client_secret=topsecret"));
        }

        String document = document(events, "uaa.jwt.validate").toString();
        assertTrue(document.contains("abcde...vwxyz"));
        assertFalse(document.contains("abcdefghijklmnopqrstuvwxyz"));
        assertFalse(document.contains("password=secret"));
        assertFalse(document.contains("otp=123456"));
        assertFalse(document.contains("access_token=rawtoken"));
        assertFalse(document.contains("client_secret=topsecret"));
    }

    @Test
    void activationSuccessAndFailureTracesAndLogsAreProduced() {
        List<ObservationEvent> events = new ArrayList<>();
        UaaObservation observation = new UaaObservation(observation(events));
        ListAppender<ILoggingEvent> appender = attachLogAppender();
        try {
            UaaObservation.OperationContext ctx = new UaaObservation.OperationContext(
                    "uaa.activation.pwa.request", "uaa.activation.pwa", "activationRequest", "started", null);
            observation.operationStarted(ctx);
            try (ObservationScope scope = observation.traceActivationPwa(ctx)) {
                scope.success();
            }
            observation.operationCompleted(ctx);

            RuntimeException failure = new RuntimeException("otp=654321 password=secret");
            try (ObservationScope scope = observation.traceActivationPwa(ctx)) {
                scope.failure(failure);
            }
            observation.operationFailed(ctx, failure);
        } finally {
            detachLogAppender(appender);
        }

        assertTrue(events.stream().anyMatch(event -> "uaa.activation.pwa.request".equals(event.document().get("span.name"))
                && "success".equals(event.document().get("event.outcome"))));
        assertTrue(events.stream().anyMatch(event -> "uaa.activation.pwa.request".equals(event.document().get("span.name"))
                && "failure".equals(event.document().get("event.outcome"))));
        assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("UAA operation started")));
        assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("UAA operation completed")));
        assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("UAA operation failed")));
    }

    @Test
    void securityAuthSpanIsChildOfControllerBoundary() {
        List<ObservationEvent> events = new ArrayList<>();
        UaaObservation observation = new UaaObservation(observation(events));

        ObservationScope controller = observation.traceController(controllerContext());
        ObservationScope auth = observation.traceAuth(new UaaObservation.AuthContext(
                "oauth2",
                "pwa",
                "default",
                "authenticate",
                "authenticate",
                "started",
                null,
                "username",
                "127.0.0.1",
                false,
                "access_token",
                null,
                null,
                "username",
                null,
                null
        ));
        auth.success().close();
        controller.success().close();

        Map<String, Object> rootDocument = document(events, "uaa.controller.login.token");
        Map<String, Object> authDocument = document(events, "uaa.auth.authenticate");
        assertEquals(rootDocument.get("trace.id"), authDocument.get("trace.id"));
        assertEquals(rootDocument.get("span.id"), authDocument.get("parent.span.id"));
    }

    private UaaObservation.ControllerContext controllerContext() {
        return new UaaObservation.ControllerContext(
                "login",
                "token",
                "POST",
                "/oauth2/token",
                200,
                "127.0.0.1",
                "corr-1",
                "started",
                null
        );
    }

    private ScmObservation observation(List<ObservationEvent> events) {
        ObservationSignalPolicy signalPolicy = signal -> signal == ObservationSignal.TRACE;
        ObservationEventSink sink = new ObservationEventSink() {
            @Override
            public ObservationEventSignal signal() {
                return ObservationEventSignal.TRACE;
            }

            @Override
            public void write(ObservationEvent event) {
                events.add(event);
            }
        };
        ObservationEventDispatcher dispatcher = new ObservationEventDispatcher(signalPolicy, List.of(sink));
        ObservationAttributeRegistry registry = new ObservationAttributeRegistry(List.of(new UaaObservationAttributeContributor()));
        SecretScrubbingObservationSanitizer sanitizer = new SecretScrubbingObservationSanitizer();
        ObservationDocumentFactory documentFactory = new ObservationDocumentFactory(null, registry, sanitizer);
        ObservationRecordValidator validator = new ObservationRecordValidator(registry);
        return new ScmObservation(
                null,
                new ObsTargetIndexResolver(),
                signalPolicy,
                dispatcher,
                null,
                new StructuredTraceObservationSink(dispatcher, documentFactory, validator, CLOCK),
                sanitizer,
                documentFactory,
                validator,
                CLOCK
        );
    }

    private Map<String, Object> document(List<ObservationEvent> events, String spanName) {
        return events.stream()
                .map(ObservationEvent::document)
                .filter(document -> spanName.equals(document.get("span.name")))
                .findFirst()
                .orElseThrow();
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(UaaObservation.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(UaaObservation.class);
        logger.detachAppender(appender);
    }
}
