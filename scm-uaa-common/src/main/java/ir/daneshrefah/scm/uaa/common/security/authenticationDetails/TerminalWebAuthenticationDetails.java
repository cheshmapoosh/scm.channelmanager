package ir.daneshrefah.scm.uaa.common.security.authenticationDetails;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.Serializable;
import java.util.*;

public class TerminalWebAuthenticationDetails extends WebAuthenticationDetails {

    @Getter
    private String clientId;
    @Getter
    private Claim claim;
    private Map<String, String> headers;

    public TerminalWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.clientId = extractClientId(request);
        headers = extractHeaders(request);
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        boolean isStepTwoInternal = checkIsStepTwo(exception);
        if (isStepTwoInternal) {
            boolean isStepTwo = extractIsStepTwo(request);
            String claimCode = extractClaimCode(request);
            String username = ((TwoStepAuthenticationRequiredException) exception).getAuthentication().getName();
            this.claim = new Claim(isStepTwo, claimCode, username);
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        return Collections.unmodifiableMap(headers);
    }

    private boolean checkIsStepTwo(Exception exception) {
        return null != exception && TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass());
    }

    private boolean extractIsStepTwo(HttpServletRequest request) {
        String s = request.getParameter("is_step_two");
        return null != s && "true".equalsIgnoreCase(s);
    }

    private String extractClaimCode(HttpServletRequest request) {
        return request.getParameter("claim_code");
    }

    private static String extractClientId(HttpServletRequest request) {
        return request.getParameter("client_id");
    }

    private static String extractTerminalCode(HttpServletRequest request) {
        return request.getParameter("terminal_code");
    }

    public String getHeader(String headerName) {
        if (StringUtils.isBlank(headerName) || Objects.isNull(headers) || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.equalsIgnoreCase(headerName, key) && StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    public record Claim(boolean isStepTwo, String claimCode, String username) implements Serializable {
    }

}
