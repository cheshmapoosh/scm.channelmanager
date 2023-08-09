package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.core.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.message.Event;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@Service
public class CompositionServiceExecutor extends ServiceExecutor {

    @Autowired
    private ServiceProducerTemplate serviceProducerTemplate;
    @Autowired
    private ServiceService serviceService;

    @Override
    protected Object executeInternal(ir.daneshrefah.scm.plugin.api.model.service.Service service, Message message,
                                     Object requestPayload) {

        CompositionService compositionService = (CompositionService) service;
        List<ServiceRelation> relations = compositionService.getRelations();
        if (null == relations) {
            relations = serviceService.findServiceRelationListBySourceServiceId(service.getId(), ServiceRelationType.COMPOSITION);
            compositionService.setRelations(relations);
        }
        Deque<ServiceRelation> reverseServiceStack = new LinkedList<>();
        Queue<ServiceRelation> commitServiceQueueQueue = new ArrayDeque<>();

        for (Iterator<ServiceRelation> iterator = relations.iterator(); iterator.hasNext(); ) {
            ServiceRelation serviceRelation = iterator.next();

            AbstractTransformer relationRequestTransformer = getTransformer(serviceRelation.getTargetServiceTransformerRequestType(),
                    serviceRelation.getTargetServiceTransformerRequestClassName());
            Object relationRequestPayload = message.getPayload();
            if (null != relationRequestTransformer) {
                relationRequestPayload = relationRequestTransformer.transform(relationRequestPayload, message,
                        serviceRelation.getTargetServiceTransformerRequestMetadata());
            }

            Message tempMessage = SerializationUtils.clone(message);
            tempMessage.setErrors(null);
            tempMessage.setEvents(null);
            tempMessage.setPayload((JsonNode) relationRequestPayload);
            serviceProducerTemplate.callService(serviceRelation.getTargetService(), tempMessage);
            message.addEvents(tempMessage.getEvents());
            message.addErrors(tempMessage.getErrors());

            AbstractTransformer relationResponseTransformer = getTransformer(serviceRelation.getTargetServiceTransformerResponseType(),
                    serviceRelation.getTargetServiceTransformerResponseClassName());
            Object relationResponsePayload = message.getPayload();
            if (null != relationResponseTransformer) {
                relationResponsePayload = relationResponseTransformer.transform(tempMessage.getPayload(), tempMessage,
                        serviceRelation.getTargetServiceTransformerResponseMetadata());
            }

            message.setPayload((JsonNode) relationResponsePayload);

            reverseServiceStack.push(serviceRelation);
            commitServiceQueueQueue.add(serviceRelation);

        }

        return message.getPayload();
    }
}
