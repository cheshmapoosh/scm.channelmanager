package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
public class TransactionAuthenticationInterceptor extends MessageInterceptor {

    private final AuthenticationClientTemplate authenticationClientTemplate;

    @Override
    protected Message internalIntercept(Message message) {
        MessageBuildRequest request = message.getHeader().getRequest();
        ClientAuthenticationRequest authenticationRequest = ClientAuthenticationRequest.builder()
                .username(request.getUsername())
                .terminalCode(request.getTerminalCode())
                .authenticationType(request.getTransactionAuthenticationType())
                .authenticationValue(request.getTransactionAuthenticationValue())
                .build();
        UserAuthentication authentication = authenticationClientTemplate.authenticateTransactionByAuthenticationRequest(authenticationRequest);
        message.getHeader().authenticateTransaction(null != authentication &&
                authentication.isAuthenticated() && !authentication.isAnonymous());
        if (authentication.hasError()) {
            String errorMessage = authentication.getError();
            if (StringUtils.isEmpty(errorMessage)) {
                errorMessage = "error on authenticate user.";
            }
            message.addError(new Error(Constants.SCM_PARAMETER_AUTHORIZATION,
                    ErrorCodes.ERROR_CODE_TRANSACTION_AUTHENTICATION_FAILED, errorMessage), Status.SC_UNAUTHORIZED);
        }
        return message;
    }

}
