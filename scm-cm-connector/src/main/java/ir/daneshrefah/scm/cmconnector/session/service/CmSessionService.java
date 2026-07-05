package ir.daneshrefah.scm.cmconnector.session.service;

import ir.daneshrefah.scm.cmconnector.session.mapper.CmSessionMapper;
import ir.daneshrefah.scm.cmconnector.session.model.CmSessionResponse;
import ir.daneshrefah.scm.uaa.client.session.ScmSessionAccessDeniedException;
import ir.daneshrefah.scm.uaa.client.session.ScmSessionPrincipalInvalidException;
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
        ObservationScope scope = safeStartScope();

        log.info("CM connector session read requested");
        safeObservationEvent("started", "started", null);
        try {
            ScmSessionView session = sessionReader.findCurrentSession(principal).orElse(null);
            if (session == null) {
                log.info("CM connector session read completed result=miss");
                safeScopeOutcome(scope, "miss");
                safeObservationEvent("miss", "miss", null);
                safeRecordSessionMetric("miss", startedAt);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
            }

            log.info("CM connector session read completed result=hit");
            safeScopeSuccess(scope);
            safeObservationEvent("hit", "hit", null);
            safeRecordSessionMetric("hit", startedAt);
            return sessionMapper.toResponse(session);
        } catch (ScmSessionPrincipalInvalidException exception) {
            log.warn("CM connector session principal invalid");
            safeScopeOutcome(scope, "invalid_principal");
            safeObservationEvent("invalid_principal", "invalid_principal", exception);
            safeRecordSessionMetric("invalid_principal", startedAt);
            safeRecordErrorMetric("invalid_principal");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session principal invalid");
        } catch (ScmSessionAccessDeniedException exception) {
            log.warn("CM connector session ownership denied");
            safeScopeOutcome(scope, "denied");
            safeObservationEvent("denied", "denied", exception);
            safeRecordSessionMetric("denied", startedAt);
            safeRecordErrorMetric("ownership_denied");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session ownership denied");
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("CM connector session read failed failureType={}", exception.getClass().getSimpleName());
            safeScopeFailure(scope, exception);
            safeObservationEvent("failure", "failure", exception);
            safeRecordSessionMetric("failure", startedAt);
            safeRecordErrorMetric(exception.getClass().getSimpleName());
            throw exception;
        } finally {
            safeCloseScope(scope);
        }
    }

    private ObservationScope safeStartScope() {
        try {
            return observation.trace()
                    .source(CmSessionService.class)
                    .span(OPERATION_NAME)
                    .attribute(CmConnectorTraceAttributes.OPERATION_NAME, OPERATION_NAME)
                    .start();
        } catch (RuntimeException exception) {
            log.warn("event=CM_CONNECTOR_TRACE_SCOPE_FAILED outcome=ignored operation={} failureType={}",
                    OPERATION_NAME,
                    exception.getClass().getSimpleName());
            return null;
        }
    }

    private void safeScopeOutcome(ObservationScope scope, String outcome) {
        if (scope == null) {
            return;
        }
        try {
            scope.outcome(outcome);
        } catch (RuntimeException exception) {
            log.warn("event=CM_CONNECTOR_TRACE_SCOPE_OUTCOME_FAILED outcome=ignored operation={} traceOutcome={} failureType={}",
                    OPERATION_NAME,
                    outcome,
                    exception.getClass().getSimpleName());
        }
    }

    private void safeScopeSuccess(ObservationScope scope) {
        if (scope == null) {
            return;
        }
        try {
            scope.success();
        } catch (RuntimeException exception) {
            log.warn("event=CM_CONNECTOR_TRACE_SCOPE_SUCCESS_FAILED outcome=ignored operation={} failureType={}",
                    OPERATION_NAME,
                    exception.getClass().getSimpleName());
        }
    }

    private void safeScopeFailure(ObservationScope scope, RuntimeException businessException) {
        if (scope == null) {
            return;
        }
        try {
            scope.failure(businessException);
        } catch (RuntimeException exception) {
            log.warn("event=CM_CONNECTOR_TRACE_SCOPE_FAILURE_FAILED outcome=ignored operation={} failureType={}",
                    OPERATION_NAME,
                    exception.getClass().getSimpleName());
        }
    }

    private void safeCloseScope(ObservationScope scope) {
        if (scope == null) {
            return;
        }
        try {
            scope.close();
        } catch (RuntimeException exception) {
            log.warn("event=CM_CONNECTOR_TRACE_SCOPE_CLOSE_FAILED outcome=ignored operation={} failureType={}",
                    OPERATION_NAME,
                    exception.getClass().getSimpleName());
        }
    }

    private void safeObservationEvent(String action, String outcome, RuntimeException exception) {
        try {
            writeObservationEvent(action, outcome, exception);
        } catch (RuntimeException observationException) {
            log.warn("event=CM_CONNECTOR_OBSERVATION_EVENT_FAILED outcome=ignored operation={} action={} failureType={}",
                    OPERATION_NAME,
                    action,
                    observationException.getClass().getSimpleName());
        }
    }

    private void safeRecordSessionMetric(String outcome, long startedAt) {
        try {
            recordSessionMetric(outcome, startedAt);
        } catch (RuntimeException metricException) {
            log.warn("event=CM_CONNECTOR_METRIC_RECORD_FAILED outcome=ignored operation={} metricOutcome={} failureType={}",
                    OPERATION_NAME,
                    outcome,
                    metricException.getClass().getSimpleName());
        }
    }

    private void safeRecordErrorMetric(String errorCode) {
        try {
            recordErrorMetric(errorCode);
        } catch (RuntimeException metricException) {
            log.warn("event=CM_CONNECTOR_ERROR_METRIC_RECORD_FAILED outcome=ignored operation={} errorCode={} failureType={}",
                    OPERATION_NAME,
                    errorCode,
                    metricException.getClass().getSimpleName());
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
