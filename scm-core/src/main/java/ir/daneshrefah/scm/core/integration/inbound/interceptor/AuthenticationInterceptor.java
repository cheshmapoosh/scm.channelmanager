package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageRequestInfo;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
public class AuthenticationInterceptor extends MessageInterceptor {

    private final AuthenticationClientTemplate authenticationClientTemplate;

    @Override
    protected Message internalIntercept(Message message) {
        if (Objects.nonNull(message.getHeader().getAuthentication())) {
            return message;
        }
        MessageRequestInfo request = message.getHeader().getRequest();
        ClientAuthenticationRequest authenticationRequest = ClientAuthenticationRequest.builder()
                .username(request.getUsername())
                .terminalCode(request.getTerminalCode())
                .clientId(request.getClientId())
                .authenticationType(request.getAuthenticationType())
                .authenticationValue(request.getAuthenticationValue())
                .accessParameter(request.getAccessParameter())
                .build();
        UserAuthentication authentication = null;
        Exception error = null;
        try {
            authentication = authenticationClientTemplate.authenticateUserByAuthenticationRequest(authenticationRequest);
        } catch (ClientAuthenticationException e) {
            authentication = e.getAuthentication();
            error = null != e.getCause() ? (Exception) e.getCause() : e;
        } catch (Exception e) {
            throw e;
        }
        message.getHeader().authenticate(authentication);
        if (authentication.hasError() || null != error) {
            String errorMessage = null != error ? error.getMessage() : authentication.getError();
            if (StringUtils.isEmpty(errorMessage)) {
                errorMessage = "error on authenticate user.";
            }
            message.addError(new Error(Constants.SCM_PARAMETER_AUTHORIZATION,
                    ErrorCodes.ERROR_CODE_AUTHENTICATION_FAILED, errorMessage), MessageStatus.SC_UNAUTHORIZED);
        }
//        logAuthenticationEvent(authenticationRequest, message, authentication, error, startTime);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //TODO {RAYANI} remove above line
        return message;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return serviceAccess.getTerminal().isSupportCheckAuthentication() &&
                serviceAccess.getService().getCheckAccessFirstAuthentication();
    }

}
