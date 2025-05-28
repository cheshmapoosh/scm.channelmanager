package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@Component
@RequiredArgsConstructor
public class TransactionAuthenticationInterceptor extends MessageInterceptor {

    private final AuthenticationClientTemplate authenticationClientTemplate;

    @Override
    protected Message internalIntercept(Message message) {
        if (AuthenticationUtils.isTransactionAuthenticationInitialized()) {
            return message;
        }
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        ClientAuthenticationRequest authenticationRequest = ClientAuthenticationRequest.builder()
                .username(messageInput.getUsername())
                .terminalCode(messageInput.getTerminalCode())
                .tokenType(messageInput.getTransactionAuthenticationType())
                .authenticationValue(messageInput.getTransactionAuthenticationValue())
                .build();
        UserAuthentication authentication = null;
        Exception error = null;
        try {
            authentication = authenticationClientTemplate.authenticateTransactionByAuthenticationRequest(authenticationRequest);
        } catch (ClientAuthenticationException e) {
            authentication = e.getAuthentication();
            error = null != e.getCause() ? (Exception) e.getCause() : e;
        } catch (Exception e) {
            throw e;
        }
        AuthenticationUtils.authenticateTransaction(null != authentication &&
                authentication.isAuthenticated() && !authentication.isAnonymous());
        if (authentication.hasError() || null != error) {
            String errorMessage = null != error ? error.getMessage() : authentication.getError();
            if (StringUtils.isEmpty(errorMessage)) {
                errorMessage = "error on authenticate user.";
            }
            message.addError(new Error(Constants.SCM_PARAMETER_AUTHORIZATION,
                    ErrorCodes.ERROR_CODE_TRANSACTION_AUTHENTICATION_FAILED, errorMessage), MessageStatus.SC_UNAUTHORIZED);
        }
//        logAuthenticationEvent(authenticationRequest, message, authentication, error, startTime);
        return message;
    }

    @Override
    protected boolean support(ScmService service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return terminal.isSupportCheckSecondAuthentication() &&
                service.getCheckAccessSecondAuthentication();
    }

    @Override
    public InterceptorConfig interceptorConfig() {
        return InterceptorConfig
                .create()
                .order(3)
                .type(InterceptorConfig.Type.REQUEST)
                .build();
    }

}
