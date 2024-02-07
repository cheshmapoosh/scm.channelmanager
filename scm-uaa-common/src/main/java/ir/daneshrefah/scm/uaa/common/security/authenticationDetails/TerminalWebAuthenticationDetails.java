package ir.daneshrefah.scm.uaa.common.security.authenticationDetails;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.Serializable;

public class TerminalWebAuthenticationDetails extends WebAuthenticationDetails {

    @Getter
    private String clientId;
    @Getter
    private Claim claim;

    public TerminalWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.clientId = extractClientId(request);
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        boolean isStepTwoInternal = checkIsStepTwo(exception);
        if (isStepTwoInternal) {
            boolean isStepTwo = extractIsStepTwo(request);
            String claimCode = extractClaimCode(request);
            String username = ((TwoStepAuthenticationRequiredException) exception).getAuthentication().getName();
            this.claim = new Claim(isStepTwo, claimCode, username);
        }
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

    public record Claim(boolean isStepTwo, String claimCode, String username) implements Serializable {
    }
}
