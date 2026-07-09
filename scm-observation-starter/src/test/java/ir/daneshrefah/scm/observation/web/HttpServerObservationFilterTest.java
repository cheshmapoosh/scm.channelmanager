package ir.daneshrefah.scm.observation.web;

import ir.daneshrefah.scm.observation.starter.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.starter.ObservationEvent;
import ir.daneshrefah.scm.observation.starter.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.starter.ObservationEventSignal;
import ir.daneshrefah.scm.observation.starter.ObservationEventSink;
import ir.daneshrefah.scm.observation.starter.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.TraceContextHolder;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.starter.trace.StructuredTraceObservationSink;
import ir.daneshrefah.scm.observation.starter.web.HttpServerObservationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HttpServerObservationFilterTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-29T00:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clearContext() {
        TraceContextHolder.clear();
    }

    @Test
    void createsOneRootTraceSpanForHttpRequest() throws Exception {
        List<ObservationEvent> events = new ArrayList<>();
        HttpServerObservationFilter filter = new HttpServerObservationFilter(
                observation(events),
                signal -> signal == ObservationSignal.TRACE,
                "uaa.http.request"
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth2/token");
        request.addHeader("X-Correlation-ID", "corr-1");
        request.setRemoteAddr("10.0.0.1");
        request.setAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern", "/oauth2/token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_CREATED);

        filter.doFilter(request, response, chain);

        assertEquals(1, events.size());
        Map<String, Object> document = events.get(0).document();
        assertEquals("uaa.http.request", document.get("span.name"));
        assertEquals("server", document.get("span.kind"));
        assertEquals("request", document.get("correlation.type"));
        assertEquals("corr-1", document.get("correlation.id"));
        assertEquals("POST", document.get("http.method"));
        assertEquals("/oauth2/token", document.get("http.route"));
        assertEquals("/oauth2/token", document.get("url.path"));
        assertEquals(201, document.get("http.status_code"));
        assertEquals("success", document.get("event.outcome"));
        assertFalse(document.containsKey("parent.span.id"));
    }

    @Test
    void handledExceptionMarksCurrentHttpSpanFailureWithoutSecondSpan() throws Exception {
        List<ObservationEvent> events = new ArrayList<>();
        HttpServerObservationFilter filter = new HttpServerObservationFilter(
                observation(events),
                signal -> signal == ObservationSignal.TRACE,
                "uaa.http.request"
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/uaa/error");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) -> {
            HttpServerObservationFilter.recordException(servletRequest,
                    new IllegalStateException("password=secret otp=123456"));
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        };

        filter.doFilter(request, response, chain);

        assertEquals(1, events.size());
        Map<String, Object> document = events.get(0).document();
        assertEquals("failure", document.get("event.outcome"));
        assertEquals("java.lang.IllegalStateException", document.get("error.type"));
        assertFalse(String.valueOf(document.get("error.message")).contains("password=secret"));
        assertFalse(String.valueOf(document.get("error.message")).contains("otp=123456"));
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
        ObservationAttributeRegistry registry = ObservationAttributeRegistry.commonOnly();
        SecretScrubbingObservationSanitizer sanitizer = new SecretScrubbingObservationSanitizer();
        ObservationDocumentFactory documentFactory = new ObservationDocumentFactory(context(), registry, sanitizer);
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

    private ObservationContext context() {
        return new ObservationContext(
                true,
                "scm",
                "test",
                "test-service",
                "test",
                "default",
                "default",
                "default",
                "1.0.0",
                "standalone",
                ZoneOffset.UTC
        );
    }
}
