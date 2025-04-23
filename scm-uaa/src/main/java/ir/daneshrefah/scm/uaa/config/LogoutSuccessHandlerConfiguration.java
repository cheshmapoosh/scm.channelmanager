package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.service.logout.LogoutService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutSuccessHandlerConfiguration implements LogoutSuccessHandler {

    private final LogoutService logoutService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String redirectUri = request.getParameter("redirect_uri");
        String clientId = request.getParameter("client_id");
        String responseType = request.getParameter("response_type");
        String scope = request.getParameter("scope");
        response.setStatus(HttpServletResponse.SC_OK);
        if (!(StringUtils.isEmpty(responseType) || StringUtils.isEmpty(redirectUri) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(scope))) {
            String serverHost = request.getRequestURL().toString().split("/logout")[0];
            String redirection = serverHost +
                    "/oauth2/authorize?response_type=" + responseType +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&scope=" + scope;
            response.sendRedirect(redirection);
        }
        logoutService.sendLogoutMessage(authentication);
    }
}