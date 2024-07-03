package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
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
        Authentication authentication = AuthenticationUtils.getScmAuthentication();
        if (Objects.nonNull(authentication)) {
            return message;
        }
        MessageInput messageInput = message.getHeader().getInput();
        ClientAuthenticationRequest authenticationRequest = ClientAuthenticationRequest.builder()
                .username(messageInput.getUsername())
                .terminalCode(messageInput.getTerminalCode())
                .clientId(messageInput.getClientId())
                .authenticationType(messageInput.getAuthenticationType())
                .authenticationValue(messageInput.getAuthenticationValue())
                .accessParameter(messageInput.getAccessParameter())
                .build();
        UserAuthentication userAuthentication = null;
        Exception error = null;
        try {
            userAuthentication = authenticationClientTemplate.authenticateUserByAuthenticationRequest(authenticationRequest);
        } catch (ClientAuthenticationException e) {
            userAuthentication = e.getAuthentication();
            error = null != e.getCause() ? (Exception) e.getCause() : e;
        } catch (Exception e) {
            throw e;
        }
        if (userAuthentication.hasError() || null != error) {
            String errorMessage = null != error ? error.getMessage() : userAuthentication.getError();
            if (StringUtils.isEmpty(errorMessage)) {
                errorMessage = "error on authenticate user.";
            }
            message.addError(new Error(Constants.SCM_PARAMETER_AUTHORIZATION,
                    ErrorCodes.ERROR_CODE_AUTHENTICATION_FAILED, errorMessage), MessageStatus.SC_UNAUTHORIZED);
        }
//        logAuthenticationEvent(authenticationRequest, message, authentication, error, startTime);
        SecurityContextHolder.getContext().setAuthentication(userAuthentication);
        return message;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return serviceAccess.getTerminal().isSupportCheckAuthentication() &&
                serviceAccess.getService().getCheckAccessFirstAuthentication();
    }

}
