package ir.daneshrefah.scm.plugin.api.model.message;

import ir.daneshrefah.scm.common.model.message.ClientAuthenticationType;
import ir.daneshrefah.scm.common.model.message.HttpMessageInput;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

import static ir.daneshrefah.scm.utils.constant.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_METHOD_OPTIONS;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
@RequiredArgsConstructor
public class DefaultHttpMessageRequestBuilder {

    private final HttpServletRequest request;
    private final String serviceCode;

    public MessageBuildRequest<HttpMessageInput> build() {
        MessageBuildRequest<HttpMessageInput> result = new MessageBuildRequest();
        try {
            result.setInput(buildMessageInput(request));
            result.setTerminalCode(request.getHeader(SCM_PARAMETER_TERMINAL));
            result.setServiceCode(serviceCode);
            result.setContentType(request.getContentType());
            result.setClientRemoteAddress(request.getRemoteAddr());
            result.setClientCorrelationId(request.getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID));
            result.setClientTimestamp(null != request.getHeader(SCM_PARAMETER_CLIENT_TIMESTAMP) ?
                    Instant.parse(request.getHeader(SCM_PARAMETER_CLIENT_TIMESTAMP)) : null);
            result.setClientAgent(request.getHeader(HttpConstants.HTTP_HEADER_USER_AGENT));
            result.setUsername(request.getHeader(SCM_PARAMETER_USERNAME));
            result.setAccessParameter(request.getHeader(SCM_PARAMETER_ACCESS_PARAMETER));
            result.setForCheck(HTTP_METHOD_OPTIONS.equalsIgnoreCase(request.getMethod()));
            result.setReceiveTimestamp(Instant.now());
            result.setServerHost(request.getHeader(HttpConstants.HTTP_HEADER_HOST));
            String authorizationHeader = request.getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION);
            result.setAuthenticationType(extractAuthenticationType(authorizationHeader));
            result.setAuthenticationValue(extractAuthenticationValue(authorizationHeader));
            String transactionValue = request.getHeader(SCM_PARAMETER_CLAIM_CODE);
            result.setTransactionAuthenticationType(StringUtils.isEmpty(transactionValue) ?
                    ClientAuthenticationType.ANONYMOUS : ClientAuthenticationType.BASIC);
            result.setTransactionAuthenticationValue(transactionValue);
//            result.setPayload(extractMessagePayload(input, service));
        } catch (Exception e) {
            result.setError(e);
        }
        return result;
    }

    private HttpMessageInput buildMessageInput(HttpServletRequest request) {
        return null;
    }

    @Builder
    public static DefaultHttpMessageRequestBuilder builder(HttpServletRequest request, String serviceCode) {
        return new DefaultHttpMessageRequestBuilder(request, serviceCode);
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
