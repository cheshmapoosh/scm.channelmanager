package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@Component
public class TerminalRequestTransformerInterceptor extends MessageInterceptor {

    @Override
    protected Message internalIntercept(Message message) {
        return message;
    }

    @Override
    protected boolean support(ScmService service) {
        return true;
    }

    @Override
    public InterceptorConfig interceptorConfig() {
        return InterceptorConfig
                .create()
                .order(4)
                .type(InterceptorConfig.Type.REQUEST)
                .build();
    }

}
