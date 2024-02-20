package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.exception.DisableServiceExecutionException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class RequestValidationInterceptor extends MessageInterceptor {

    @Override
    protected Message internalIntercept(Message message) {
        Service service = message.getHeader().getServiceAccess().getService();
        if (!ServiceStatus.ACTIVE.equals(service.getStatus())) {
            throw new DisableServiceExecutionException(service);
        }
        return message;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return true;
    }

}
