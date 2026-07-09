package ir.daneshrefah.scm.cmconnector.otp.service;

import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyRequest;
import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyResponse;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorMetricTags;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorTraceAttributes;
import ir.daneshrefah.scm.cmconnector.observation.CmConnectorMetricNames;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.uaa.starter.security.ScmPrincipal;
import ir.daneshrefah.scm.uaa.starter.security.ScmSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmOtpService {
    private static final String OPERATION_NAME = "scm.cm.connector.otp.verify";
    private static final String EVENT_CATEGORY = "cm.otp";

    private final ScmSecurityContext securityContext;
    private final OtpVerificationGateway otpVerificationGateway;
    private final ScmObservation observation;

    public CmOtpVerifyResponse verify(CmOtpVerifyRequest request) {
        long startedAt = System.nanoTime();
        ScmPrincipal principal = securityContext.requirePrincipal();
        ObservationScope scope = safeStartScope();

        log.info("CM connector OTP verification delegated");
        safeObservationEvent("started", "started", null);
        try {
            CmOtpVerifyResponse response = otpVerificationGateway.verify(principal, request);
            String outcome = response.verified() ? "verified" : "rejected";
            log.info("CM connector OTP verification completed result={}", outcome);
            safeScopeOutcome(scope, outcome);
            safeObservationEvent(outcome, outcome, null);
            safeRecordOtpMetric(outcome, startedAt);
            return response;
        } catch (RuntimeException exception) {
            log.error("CM connector OTP verification failed failureType={}", exception.getClass().getSimpleName());
            safeScopeFailure(scope, exception);
            safeObservationEvent("failure", "failure", exception);
            safeRecordOtpMetric("failure", startedAt);
            safeRecordErrorMetric(exception.getClass().getSimpleName());
            throw exception;
        } finally {
            safeCloseScope(scope);
        }
    }

    private ObservationScope safeStartScope() {
        try {
            return observation.trace()
                    .source(CmOtpService.class)
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

    private void safeRecordOtpMetric(String outcome, long startedAt) {
        try {
            recordOtpMetric(outcome, startedAt);
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

    private void recordOtpMetric(String outcome, long startedAt) {
        observation.metric()
                .timer(CmConnectorMetricNames.OTP_VERIFY)
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
                .source(CmOtpService.class)
                .loggerName(CmOtpService.class)
                .message("CM connector OTP verification event")
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
