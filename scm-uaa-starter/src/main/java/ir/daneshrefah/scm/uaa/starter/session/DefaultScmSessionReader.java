package ir.daneshrefah.scm.uaa.starter.session;

import ir.daneshrefah.scm.uaa.starter.security.ScmPrincipal;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class DefaultScmSessionReader implements ScmSessionReader {
    private final SessionCache sessionCache;

    public DefaultScmSessionReader(SessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    @Override
    public Optional<ScmSessionView> findCurrentSession(ScmPrincipal principal) {
        if (principal == null
                || !StringUtils.hasText(principal.nickname())
                || !StringUtils.hasText(principal.terminalCode())) {
            throw new ScmSessionPrincipalInvalidException();
        }

        UserAuthentication authentication = sessionCache.getSessionFromCache(
                principal.nickname(),
                principal.terminalCode()
        );
        if (authentication == null) {
            return Optional.empty();
        }
        if (!ownedByPrincipal(authentication, principal)) {
            throw new ScmSessionAccessDeniedException();
        }
        return Optional.of(toView(authentication));
    }

    private boolean ownedByPrincipal(UserAuthentication authentication, ScmPrincipal principal) {
        if (!matches(authentication.getName(), principal.nickname())) {
            return false;
        }
        if (!matches(authentication.getTerminalCode(), principal.terminalCode())) {
            return false;
        }

        String cachedSessionId = authentication.getDetails() == null
                ? null
                : authentication.getDetails().getSessionId();
        if (StringUtils.hasText(cachedSessionId) && StringUtils.hasText(principal.sessionId())) {
            return matches(cachedSessionId, principal.sessionId());
        }
        return true;
    }

    private ScmSessionView toView(UserAuthentication authentication) {
        UserAuthentication.AuthenticationDetail details = authentication.getDetails();
        if (details == null) {
            throw new IllegalStateException("Cached session is missing authentication details");
        }
        return new ScmSessionView(
                details.getIssuer(),
                details.getSessionId(),
                details.getIssuedAt(),
                details.getExpiresAt(),
                details.getMaxIdle(),
                authentication.getAuthenticationMethod() == null ? null : authentication.getAuthenticationMethod().name(),
                details.getLoginAccessParameter()
        );
    }

    private boolean matches(String expected, String actual) {
        return StringUtils.hasText(expected)
                && StringUtils.hasText(actual)
                && expected.trim().equals(actual.trim());
    }
}
