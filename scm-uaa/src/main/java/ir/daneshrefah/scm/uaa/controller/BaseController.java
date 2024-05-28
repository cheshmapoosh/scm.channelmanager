package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.user.AuthenticationLevel;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.USERNAME_ANONYMOUS;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.IP_HEADER;
import static ir.daneshrefah.scm.utils.constant.Constants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
public abstract class BaseController {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected String extractRequestTerminalCode() {
        UserAuthentication userAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.nonNull(userAuthentication) && userAuthentication.isFullyAuthenticated()) {
            return userAuthentication.getTerminalCode();
        }
        HttpServletRequest request = extractHttpRequest();
        if (Objects.isNull(request)) {
            return null;
        }
        return extractRequestTerminalCode(request);
    }

    protected String extractRequestTerminalCode(HttpServletRequest request) {
        return request.getHeader(SCM_PARAMETER_TERMINAL);
    }

    protected Optional<String> extractRequestAccessParameter() {
        HttpServletRequest request = extractHttpRequest();
        if (Objects.isNull(request)) {
            return Optional.empty();
        }
        return extractRequestAccessParameter(request);
    }

    protected Optional<String> extractRequestAccessParameter(HttpServletRequest request) {
        String header = request.getHeader(SCM_PARAMETER_ACCESS_PARAMETER);
        if (StringUtils.isBlank(header)) {
            return Optional.empty();
        }
        return Optional.of(header);
    }

    protected IssuerInfo extractIssuerInfo() {
        HttpServletRequest request = extractHttpRequest();
        if (Objects.isNull(request)) {
            return null;
        }
        return extractIssuerInfo(request);
    }

    protected IssuerInfo extractIssuerInfo(HttpServletRequest request) {
        UserAuthentication userAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        IssuerInfo result = IssuerInfo.builder()
                .parentCorrelationId(Objects.nonNull(request) ? request.getHeader(SCM_PARAMETER_CORRELATION_ID) : null)
                .authenticationLevel(extractCurrentUserAuthenticationLevel())
                .identifier(extractCurrentUserIdentifier())
                .identifierType(extractCurrentUserIdentifierType())
                .terminalCode(Objects.nonNull(userAuthentication) && userAuthentication.isFullyAuthenticated() &&
                        Objects.nonNull(userAuthentication.getPrincipal()) ?
                        userAuthentication.getPrincipal().getTerminalCode() : request.getHeader(SCM_PARAMETER_TERMINAL))
                .accessParameter(request.getHeader(SCM_PARAMETER_TERMINAL))
                .remoteAddress(Objects.nonNull(request) ? request.getRemoteHost() : null)
                .xForwardedFor(Objects.nonNull(request) ? request.getHeader(IP_HEADER) : null)
                .hostAddress(getLocalHostAddress())
//        private String instanceName;
                .build();
        return result;
    }

    public AuthenticationLevel extractCurrentUserAuthenticationLevel() {
        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.nonNull(user) && user.isFullyAuthenticated()) {
            return AuthenticationLevel.CM_AUTHENTICATED;
        }
        return AuthenticationLevel.ANONYMOUS;
    }

    public UserIdentifierType extractCurrentUserIdentifierType() {
        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.isNull(user) || !user.isFullyAuthenticated()) {
            return UserIdentifierType.NONE;
        }
        if (Objects.nonNull(user.getPrincipal())) {
            return UserIdentifierType.USER_NICKNAME;
        }
        if (Objects.nonNull(user.getPrincipal()) && Objects.nonNull(user.getPrincipal().getPerson())) {
            return UserIdentifierType.PERSON_USERNAME;
        }
        return UserIdentifierType.NONE;
    }

    public String extractCurrentUserIdentifier() {
        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
        UserIdentifierType identifierType = extractCurrentUserIdentifierType();
        if (Objects.isNull(identifierType) || UserIdentifierType.NONE.equals(identifierType)) {
            return USERNAME_ANONYMOUS;
        }
        if (UserIdentifierType.USER_NICKNAME.equals(identifierType)) {
            return user.getPrincipal().getNickname();
        }
        if (UserIdentifierType.PERSON_USERNAME.equals(identifierType)) {
            return user.getPrincipal().getPerson().getUsername();
        }
        return null;
    }

    private HttpServletRequest extractHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes) && attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }

    private String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error("error on extract host address", e);
        }
        return null;
    }

}
