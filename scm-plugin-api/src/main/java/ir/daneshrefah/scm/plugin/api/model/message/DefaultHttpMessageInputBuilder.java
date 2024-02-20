package ir.daneshrefah.scm.plugin.api.model.message;

import ir.daneshrefah.scm.common.model.message.ClientAuthenticationType;
import ir.daneshrefah.scm.common.model.message.HttpMessageInput;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_METHOD_OPTIONS;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
@RequiredArgsConstructor
public class DefaultHttpMessageInputBuilder {

    private final HttpServletRequest request;
    private final String serviceCode;

    public HttpMessageInput build() {
        HttpMessageInput result = HttpMessageInput.builder()
//                .headers(headers)
//                .body(body)
                .contentType(request.getContentType())
                .clientRemoteAddress(request.getRemoteAddr())
                .clientAgent(request.getHeader(HttpConstants.HTTP_HEADER_USER_AGENT))
                .authorization(request.getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION))
                .serverHost(request.getHeader(HttpConstants.HTTP_HEADER_HOST))
                .isForCheck(HTTP_METHOD_OPTIONS.equalsIgnoreCase(request.getMethod()))
                .serviceCode(serviceCode)
//                .httpUrl(CamelUtils.getHttpUrlFromExchange(input))
                .httpMethod(request.getMethod())
                .build();

        return result;
    }

    private HttpMessageInput buildMessageInput(HttpServletRequest request) {
        return null;
    }

    @Builder
    public static DefaultHttpMessageInputBuilder builder(HttpServletRequest request, String serviceCode) {
        return new DefaultHttpMessageInputBuilder(request, serviceCode);
    }

    private ClientAuthenticationType extractAuthenticationType(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return ClientAuthenticationType.ANONYMOUS;
        }

        String AUTHENTICATION_SCHEME_BASIC = "Basic";
        String AUTHENTICATION_SCHEME_BEARER = "Bearer";
        String AUTHENTICATION_SCHEME_SESSION = "Session";

        if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BASIC)) {
            return ClientAuthenticationType.BASIC;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_SESSION)) {
            return ClientAuthenticationType.SESSION;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BEARER)) {
            return ClientAuthenticationType.BEARER;
        }
        return ClientAuthenticationType.ANONYMOUS;
    }

    private String extractAuthenticationValue(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        String[] args = authorizationHeader.split(" ");
        if (args.length < 2)
            return null;
        return args[1];
    }

}
