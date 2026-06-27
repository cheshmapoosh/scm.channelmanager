package ir.daneshrefah.scm.cmconnector.otp.service;

import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyRequest;
import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyResponse;
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
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmOtpService {
    private final ScmSecurityContext securityContext;
    private final OtpVerificationGateway otpVerificationGateway;
    private final ScmObservation observation;

    public CmOtpVerifyResponse verify(CmOtpVerifyRequest request) {
        long startedAt = System.nanoTime();
        ScmPrincipal principal = securityContext.requirePrincipal();
        ObservationScope scope = observation.trace()
                .source(CmOtpService.class)
                .span("scm.cm.connector.otp.verify")
                .attribute(CmConnectorTraceAttributes.OPERATION_NAME, "scm.cm.connector.otp.verify")
                .start();

        log.info("CM connector OTP verification delegated");
        try {
            CmOtpVerifyResponse response = otpVerificationGateway.verify(principal, request);
            String outcome = response.verified() ? "verified" : "rejected";
            log.info("CM connector OTP verification completed result={}", outcome);
            scope.outcome(outcome);
            recordOtpMetric(outcome, startedAt);
            return response;
        } catch (RuntimeException exception) {
            log.error("CM connector OTP verification failed", exception);
            scope.failure(exception);
            recordOtpMetric("failure", startedAt);
            recordErrorMetric(exception.getClass().getSimpleName());
            throw exception;
        } finally {
            scope.close();
        }
    }

    private void recordOtpMetric(String outcome, long startedAt) {
        observation.metric()
                .timer(CmConnectorMetricNames.OTP_VERIFY)
                .tag(CmConnectorMetricTags.OPERATION_NAME, "scm.cm.connector.otp.verify")
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(elapsedMillis(startedAt), TimeUnit.MILLISECONDS);
    }

    private void recordErrorMetric(String errorCode) {
        observation.metric()
                .counter(CmConnectorMetricNames.ERRORS)
                .tag(CmConnectorMetricTags.OPERATION_NAME, "scm.cm.connector.otp.verify")
                .tag(CommonMetricTags.ERROR_CODE, errorCode)
                .increment();
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
