package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.common.service.LogoutService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutSuccessHandlerConfiguration implements LogoutSuccessHandler {

    private final LogoutService logoutService;

    private final JwtDecoder jwtDecoder;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String redirectUri = request.getParameter("redirect_uri");
        String clientId = request.getParameter("client_id");
        String responseType = request.getParameter("response_type");
        String scope = request.getParameter("scope");
        response.setStatus(HttpServletResponse.SC_OK);
        String jwtStr = request.getHeader("authorization");
        if (StringUtils.isNotBlank(jwtStr)) {
            Jwt decode =jwtDecoder.decode(jwtStr.replace("Bearer ",""));
            String username = decode.getClaim("sub");
            String terminal = decode.getClaim("trm");
            logoutService.sendLogoutMessage(username, terminal);
        } else if (authentication != null) {
            logoutService.sendLogoutMessage(authentication);
        }
        if (!(StringUtils.isEmpty(responseType) || StringUtils.isEmpty(redirectUri) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(scope))) {
            String serverHost = request.getRequestURL().toString().split("/logout")[0];
            String redirection = serverHost +
                    "/oauth2/authorize?response_type=" + responseType +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&scope=" + scope;
            response.sendRedirect(redirection);
        }
    }
}