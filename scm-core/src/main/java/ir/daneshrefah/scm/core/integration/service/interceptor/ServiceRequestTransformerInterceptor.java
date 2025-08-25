package ir.daneshrefah.scm.core.integration.service.interceptor;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.services.TransformerService;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
@Component
@Deprecated
public class ServiceRequestTransformerInterceptor extends AbstractTransformerInterceptor {

    public ServiceRequestTransformerInterceptor(TransformerService transformerService) {
        super(transformerService);
    }

    @Override
    protected TransformerRelationType extractTransformerRelationType() {
        return TransformerRelationType.SERVICE_REQUEST;
    }

    @Override
    public InterceptorConfig interceptorConfig() {
        return InterceptorConfig
                .create()
                .order(8)
                .type(InterceptorConfig.Type.REQUEST)
                .build();
    }
}
