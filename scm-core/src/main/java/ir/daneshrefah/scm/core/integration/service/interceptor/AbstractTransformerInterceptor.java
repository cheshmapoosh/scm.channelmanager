package ir.daneshrefah.scm.core.integration.service.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.services.TransformerService;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
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
@RequiredArgsConstructor
public abstract class AbstractTransformerInterceptor extends MessageInterceptor {

    private final Map<String, List<TransformerExecutionWrapper>> transformers = new HashMap<>();

    private final TransformerService transformerService;

    @Override
    protected Message internalIntercept(Message message) {
        List<TransformerExecutionWrapper> transformers = loadTransformerListIfRequired(message.getHeader().getService());
        if (null == transformers || transformers.isEmpty()) {
            return message;
        }
        return doTransform(transformers, message);
    }

    @Override
    protected boolean support(Service service) {
        return true;
    }

    private List<TransformerExecutionWrapper> loadTransformerListIfRequired(Service service) {
        TransformerRelationType relationType = extractTransformerRelationType();
        if (!transformers.containsKey(service.getCode())) {
            List<TransformerRelation> transformerRelations = transformerService.findAllTransformerRelationsBySource(
                    service.getId());
            List<TransformerExecutionWrapper> transformerList = transformerRelations.stream().filter(
                            t -> relationType.equals(t.getRelationType()))
                    .map(TransformerExecutionWrapper::new)
                    .collect(Collectors.toList());
            transformers.put(service.getCode(), transformerList);
        }
        return transformers.get(service.getCode());
    }

    protected abstract TransformerRelationType extractTransformerRelationType();

    public Message doTransform(List<TransformerExecutionWrapper> transformerRelations, Message message) {
        JsonNode payload = message.getPayload();
        for (TransformerExecutionWrapper transformerExecutionWrapper : transformerRelations) {
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        return message.payload(payload);
    }

}
