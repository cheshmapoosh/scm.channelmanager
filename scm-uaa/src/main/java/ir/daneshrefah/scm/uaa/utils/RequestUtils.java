package ir.daneshrefah.scm.uaa.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ir.daneshrefah.scm.uaa.utils.Constants.REQUEST_ATTRIBUTE_CORRELATION_ID;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ACCESS_PARAMETER;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_TERMINAL;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-29
 */
public class RequestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void enrichResponse(HttpServletResponse httpServletResponse, List<Cookie> cookies) {
        if (Objects.nonNull(cookies) &&  !cookies.isEmpty()) {
            cookies.forEach(httpServletResponse::addCookie);
        }
    }

    public static String getOrDefaultRequestIp(String defaultIp, HttpServletRequest request) {
        if (org.apache.commons.lang3.StringUtils.isBlank(defaultIp)) {
            String[] headers = {
                    "X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_X_FORWARDED_FOR",
                    "X-Real-IP"
            };
            for (String header : headers) {
                String foundIp = request.getHeader(header);
                if (foundIp != null && !foundIp.isEmpty() && !"unknown".equalsIgnoreCase(defaultIp)) {
                    return foundIp.split(",")[0].trim();
                }
            }
            return request.getRemoteAddr();
        }
        return defaultIp;
    }

//    public static IssuerInfo extractIssuerInfo() {
//        HttpServletRequest request = extractHttpRequest();
//        if (Objects.isNull(request)) {
//            return null;
//        }
//        return extractIssuerInfo(request);
//    }


//    public static IssuerInfo extractIssuerInfo(HttpServletRequest request) {
//        UserAuthentication userAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
//        IssuerInfo result = IssuerInfo.builder()
//                .parentCorrelationId(Objects.nonNull(request) ? request.getHeader(SCM_PARAMETER_CORRELATION_ID) : null)
//                .authenticationLevel(extractCurrentUserAuthenticationLevel())
//                .identifier(extractCurrentUserIdentifier())
//                .identifierType(extractCurrentUserIdentifierType())
//                .terminalCode(Objects.nonNull(userAuthentication) && userAuthentication.isFullyAuthenticated() &&
//                        Objects.nonNull(userAuthentication.getPrincipal()) ?
//                        userAuthentication.getPrincipal().getTerminalCode() : request.getHeader(SCM_PARAMETER_TERMINAL))
//                .accessParameter(request.getHeader(SCM_PARAMETER_TERMINAL))
//                .remoteAddress(Objects.nonNull(request) ? request.getRemoteHost() : null)
//                .xForwardedFor(Objects.nonNull(request) ? request.getHeader(IP_HEADER) : null)
//                .hostAddress(getLocalHostAddress())

    /// /        private String instanceName;
//                .build();
//        return result;
//    }

//    public static AuthenticationLevel extractCurrentUserAuthenticationLevel() {
//        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
//        if (Objects.nonNull(user) && user.isFullyAuthenticated()) {
//            return AuthenticationLevel.CM_AUTHENTICATED;
//        }
//        return AuthenticationLevel.ANONYMOUS;
//    }

//    public static String extractCurrentUserIdentifier() {
//        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
//        UserIdentifierType identifierType = extractCurrentUserIdentifierType();
//        if (Objects.isNull(identifierType) || UserIdentifierType.NONE.equals(identifierType)) {
//            return USERNAME_ANONYMOUS;
//        }
//        if (UserIdentifierType.USER_NICKNAME.equals(identifierType)) {
//            return user.getPrincipal().getNickname();
//        }
//        if (UserIdentifierType.PERSON_USERNAME.equals(identifierType)) {
//            return user.getPrincipal().getPerson().getUsername();
//        }
//        return null;
//    }

//    public static UserIdentifierType extractCurrentUserIdentifierType() {
//        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
//        if (Objects.isNull(user) || !user.isFullyAuthenticated()) {
//            return UserIdentifierType.NONE;
//        }
//        if (Objects.nonNull(user.getPrincipal())) {
//            return UserIdentifierType.USER_NICKNAME;
//        }
//        if (Objects.nonNull(user.getPrincipal()) && Objects.nonNull(user.getPrincipal().getPerson())) {
//            return UserIdentifierType.PERSON_USERNAME;
//        }
//        return UserIdentifierType.NONE;
//    }
    public static HttpServletRequest extractHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes) && attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }

    private static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
//            LOGGER.error("error on extract host address", e);
        }
        return null;
    }

    public static String extractRequestTerminalCode() {
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

    public static String extractRequestTerminalCode(HttpServletRequest request) {
        return request.getHeader(SCM_PARAMETER_TERMINAL);
    }

    public static String extractCorrelationId() {
        return extractRequestAttribute(REQUEST_ATTRIBUTE_CORRELATION_ID);
    }

    public static String extractRequestAttribute(String attributeName) {
        HttpServletRequest request = extractHttpRequest();
        if (Objects.isNull(request)) {
            return null;
        }
        return (String) request.getAttribute(attributeName);
    }

    public static Optional<String> extractRequestAccessParameter() {
        HttpServletRequest request = extractHttpRequest();
        if (Objects.isNull(request)) {
            return Optional.empty();
        }
        return extractRequestAccessParameter(request);
    }

    public static Optional<String> extractRequestAccessParameter(HttpServletRequest request) {
        String header = request.getHeader(SCM_PARAMETER_ACCESS_PARAMETER);
        if (StringUtils.isBlank(header)) {
            return Optional.empty();
        }
        return Optional.of(header);
    }


    public static MultiValueMap<String, String> getRequestHeaders(HttpServletRequest request) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(header -> {
            headers.add(header, request.getHeader(header));
        });
        return headers;
    }

}
