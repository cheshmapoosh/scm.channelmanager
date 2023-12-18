package ir.daneshrefah.scm.uaa.security.authenticationDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class TerminalWebAuthenticationDetails extends WebAuthenticationDetails {

    private String clientId;
    private String terminalCode;

    public TerminalWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.clientId = extractClientId(request);
        this.terminalCode = extractTerminalCode(request);
    }

    private static String extractClientId(HttpServletRequest request) {
        return request.getParameter("client_id");
    }

    private static String extractTerminalCode(HttpServletRequest request) {
        return request.getParameter("terminal_code");
    }

    public String getClientId() {
        return clientId;
    }

    public String getTerminalCode() {
        return terminalCode;
    }
}
