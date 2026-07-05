package ir.daneshrefah.scm.cmconnector.session.service;

import ir.daneshrefah.scm.cmconnector.session.mapper.CmSessionMapper;
import ir.daneshrefah.scm.cmconnector.session.model.CmSessionResponse;
import ir.daneshrefah.scm.uaa.client.session.ScmSessionAccessDeniedException;
import ir.daneshrefah.scm.uaa.client.session.ScmSessionReader;
import ir.daneshrefah.scm.uaa.client.session.ScmSessionView;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorMetricTags;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorTraceAttributes;
import ir.daneshrefah.scm.cmconnector.observation.CmConnectorMetricNames;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.uaa.client.security.ScmPrincipal;
import ir.daneshrefah.scm.uaa.client.security.ScmSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmSessionService {
    private static final String OPERATION_NAME = "scm.cm.connector.session.read";
    private static final String EVENT_CATEGORY = "cm.session";

    private final ScmSecurityContext securityContext;
    private final ScmSessionReader sessionReader;
    private final CmSessionMapper sessionMapper;
    private final ScmObservation observation;

    public CmSessionResponse currentSession() {
        long startedAt = System.nanoTime();
        ScmPrincipal principal = securityContext.requirePrincipal();
        ObservationScope scope = observation.trace()
                .source(CmSessionService.class)
                .span(OPERATION_NAME)
                .attribute(CmConnectorTraceAttributes.OPERATION_NAME, OPERATION_NAME)
                .start();

        log.info("CM connector session read requested");
        writeObservationEvent("started", "started", null);
        try {
            ScmSessionView session = sessionReader.findCurrentSession(principal).orElse(null);
            if (session == null) {
                log.info("CM connector session read completed result=miss");
                scope.outcome("miss");
                writeObservationEvent("miss", "miss", null);
                recordSessionMetric("miss", startedAt);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
            }

            log.info("CM connector session read completed result=hit");
            scope.success();
            writeObservationEvent("hit", "hit", null);
            recordSessionMetric("hit", startedAt);
            return sessionMapper.toResponse(session);
        } catch (ScmSessionAccessDeniedException exception) {
            log.warn("CM connector session ownership denied");
            scope.outcome("denied");
            writeObservationEvent("denied", "denied", exception);
            recordSessionMetric("denied", startedAt);
            recordErrorMetric("ownership_denied");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session ownership denied");
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("CM connector session read failed", exception);
            scope.failure(exception);
            writeObservationEvent("failure", "failure", exception);
            recordSessionMetric("failure", startedAt);
            recordErrorMetric(exception.getClass().getSimpleName());
            throw exception;
        } finally {
            scope.close();
        }
    }

    private void recordSessionMetric(String outcome, long startedAt) {
        observation.metric()
                .timer(CmConnectorMetricNames.SESSION_READ)
                .tag(CmConnectorMetricTags.OPERATION_NAME, OPERATION_NAME)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(elapsedMillis(startedAt), TimeUnit.MILLISECONDS);
    }

    private void recordErrorMetric(String errorCode) {
        observation.metric()
                .counter(CmConnectorMetricNames.ERRORS)
                .tag(CmConnectorMetricTags.OPERATION_NAME, OPERATION_NAME)
                .tag(CommonMetricTags.ERROR_CODE, errorCode)
                .increment();
    }

    private void writeObservationEvent(String action, String outcome, RuntimeException exception) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(CmConnectorTraceAttributes.OPERATION_NAME.name(), OPERATION_NAME);
        attributes.put("event.outcome", outcome);
        if (exception != null) {
            attributes.put("error.type", exception.getClass().getName());
            attributes.put("error.code", exception.getClass().getSimpleName());
        }

        observation.log()
                .event()
                .source(CmSessionService.class)
                .loggerName(CmSessionService.class)
                .message("CM connector session read event")
                .category(EVENT_CATEGORY)
                .action(action)
                .outcome(outcome)
                .attributes(attributes)
                .write();
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
