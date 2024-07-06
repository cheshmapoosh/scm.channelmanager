package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class TerminalRequestTransformerInterceptor extends MessageInterceptor {

    @Override
    protected Message internalIntercept(Message message) {
        return message;
    }

    @Override
    protected boolean support(Service service) {
        return true;
    }

}
