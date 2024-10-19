package ir.daneshrefah.scm.core.integration.service.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
@Component
public class ServiceResponseTransformerInterceptor extends AbstractTransformerInterceptor {

    public ServiceResponseTransformerInterceptor(TransformerService transformerService) {
        super(transformerService);
    }

    @Override
    protected TransformerRelationType extractTransformerRelationType() {
        return TransformerRelationType.SERVICE_RESPONSE;
    }

    @Override
    public InterceptorConfig interceptorConfig() {
        return InterceptorConfig
                .create()
                .order(1)
                .type(InterceptorConfig.Type.RESPONSE)
                .build();
    }
}
