package ir.daneshrefah.scm.core.integration.service.interceptor;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.utils.constant.Constants;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
public class DecisionManagerInterceptor extends MessageInterceptor {

    private final DecisionManager decisionManager;

    @Override
    protected Message internalIntercept(Message message) {
        try {
            boolean isServiceCallAllowed = decisionManager.decide(message);
            if (!isServiceCallAllowed) {
                message.addAccessDeniedError(Constants.SCM_PARAMETER_AUTHORIZATION, null, null);
            }
        } catch (AccessDeniedException e) {
            message.addAccessDeniedError(e.getSource(), e.getErrorCode(), e.getMessage());
        }
        return message;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return true;
    }

}
