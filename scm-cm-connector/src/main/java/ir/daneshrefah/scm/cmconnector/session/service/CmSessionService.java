package ir.daneshrefah.scm.cmconnector.session.service;

import ir.daneshrefah.scm.cmconnector.session.mapper.CmSessionMapper;
import ir.daneshrefah.scm.cmconnector.session.model.CmSessionResponse;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorMetricTags;
import ir.daneshrefah.scm.cmconnector.observation.attributes.CmConnectorTraceAttributes;
import ir.daneshrefah.scm.cmconnector.observation.CmConnectorMetricNames;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.uaa.client.security.ScmPrincipal;
import ir.daneshrefah.scm.uaa.client.security.ScmSecurityContext;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmSessionService {
    private final ScmSecurityContext securityContext;
    private final SessionCache sessionCache;
    private final CmSessionMapper sessionMapper;
    private final ScmObservation observation;

    public CmSessionResponse currentSession() {
        long startedAt = System.nanoTime();
        ScmPrincipal principal = securityContext.requirePrincipal();
        ObservationScope scope = observation.trace()
                .source(CmSessionService.class)
                .span("scm.cm.connector.session.read")
                .attribute(CmConnectorTraceAttributes.OPERATION_NAME, "scm.cm.connector.session.read")
                .start();

        log.info("CM connector session read requested");
        try {
            UserAuthentication result =
                    sessionCache.getSessionFromCache(principal.nickname(), principal.terminalCode());
            if (result == null) {
                log.info("CM connector session read completed result=miss");
                scope.outcome("miss");
                recordSessionMetric("miss", startedAt);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
            }
            if (!ownedByPrincipal(result, principal)) {
                log.warn("CM connector session ownership denied");
                scope.outcome("denied");
                recordSessionMetric("denied", startedAt);
                recordErrorMetric("ownership_denied");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session ownership denied");
            }

            log.info("CM connector session read completed result=hit");
            scope.success();
            recordSessionMetric("hit", startedAt);
            return sessionMapper.toResponse(result);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("CM connector session read failed", exception);
            scope.failure(exception);
            recordSessionMetric("failure", startedAt);
            recordErrorMetric(exception.getClass().getSimpleName());
            throw exception;
        } finally {
            scope.close();
        }
    }

    private boolean ownedByPrincipal(
            UserAuthentication authentication,
            ScmPrincipal principal
    ) {
        if (!matches(authentication.getName(), principal.nickname())) {
            return false;
        }
        if (!matches(authentication.getTerminalCode(), principal.terminalCode())) {
            return false;
        }
        String cachedSessionId = authentication.getDetails() == null ? null : authentication.getDetails().getSessionId();
        if (StringUtils.hasText(cachedSessionId) && StringUtils.hasText(principal.sessionId())) {
            return matches(cachedSessionId, principal.sessionId());
        }
        return true;
    }

    private boolean matches(String expected, String actual) {
        return StringUtils.hasText(expected)
                && StringUtils.hasText(actual)
                && expected.trim().equals(actual.trim());
    }

    private void recordSessionMetric(String outcome, long startedAt) {
        observation.metric()
                .timer(CmConnectorMetricNames.SESSION_READ)
                .tag(CmConnectorMetricTags.OPERATION_NAME, "scm.cm.connector.session.read")
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(elapsedMillis(startedAt), TimeUnit.MILLISECONDS);
    }

    private void recordErrorMetric(String errorCode) {
        observation.metric()
                .counter(CmConnectorMetricNames.ERRORS)
                .tag(CmConnectorMetricTags.OPERATION_NAME, "scm.cm.connector.session.read")
                .tag(CommonMetricTags.ERROR_CODE, errorCode)
                .increment();
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
