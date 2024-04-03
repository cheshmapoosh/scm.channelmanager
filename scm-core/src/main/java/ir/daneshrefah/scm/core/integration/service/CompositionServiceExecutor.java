package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceCompositionType;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@RequiredArgsConstructor
@Service
public class CompositionServiceExecutor extends ServiceExecutor {

    private final ServiceProducerTemplate serviceProducerTemplate;
    private final ServiceServiceImpl serviceService;
    private final TransformerService transformerService;
    private final TerminalService terminalService;
    private final Map<String, CompositeServiceExecutionWrapper> serviceExecutionMap = new HashMap<>();

    @Override
    protected JsonNode executeInternal(ir.daneshrefah.scm.common.model.service.Service service, Message message) {

        CompositionService compositionService = (CompositionService) service;
        List<ServiceRelation> relations = compositionService.getRelations();
        if (null == relations) {
            relations = serviceService.findServiceRelationListBySourceServiceId(service.getId());
            compositionService.setRelations(relations);
        }
        Deque<ServiceRelation> reverseServiceStack = new LinkedList<>();
        Queue<ServiceRelation> commitServiceQueueQueue = new ArrayDeque<>();

        for (Iterator<ServiceRelation> iterator = relations.iterator(); iterator.hasNext(); ) {
            ServiceRelation serviceRelation = iterator.next();
            CompositeServiceExecutionWrapper serviceExecutionWrapper = prepareServiceExecutionWrapper(serviceRelation);

            Object relationRequestPayload = null;
            try {
                relationRequestPayload = transformRequest(serviceExecutionWrapper.getTargetServiceRequestTransformers(), message);
            } catch (Exception e) {
                errorHandlerService.resolveMessageByException(message, e);
                break;
            }

            String terminalCode = message.getHeader().getTerminalCode();
            String serviceCode = serviceRelation.getTargetService().getCode();
            Optional<TerminalServiceAccess> serviceAccess = terminalService
                    .findTerminalServiceAccessByTerminalCodeAndServiceCode(terminalCode, serviceCode);
            if (!serviceAccess.isPresent()) {
                throw new TerminalServiceNotFoundException(terminalCode, serviceCode);
            }
            Message tempMessage = MessageUtils.generateInternalMessage(message, serviceAccess.get(), (JsonNode) relationRequestPayload);
            serviceProducerTemplate.callService(serviceRelation.getTargetService(), tempMessage);
//            Object relationResponsePayload = transformResponse(serviceExecutionWrapper.getTargetServiceResponseTransformers(), message, tempMessage.getPayload());
//            AbstractTransformer relationResponseTransformer = getTransformer(serviceRelation.getTargetServiceTransformerResponseType(),
//                    serviceRelation.getTargetServiceTransformerResponseClassName());
//            Object relationResponsePayload = tempMessage.getPayload();
//            if (null != relationResponseTransformer) {
//                relationResponsePayload = relationResponseTransformer.transform(tempMessage.getPayload(), tempMessage,
//                        serviceRelation.getTargetServiceTransformerResponseMetadata());
//            }
            if (ServiceCompositionType.AGGREGATE.equals(compositionService.getCompositionType())) {
                message.addErrors(tempMessage.getErrors(), tempMessage.getStatus());
                if (MessageStatus.SC_SUCCESS.equals(tempMessage.getStatus())) {
                    message.appendPayload(tempMessage.getPayload());
                }
            } else if (ServiceCompositionType.FIRST_RESPONSE.equals(compositionService.getCompositionType())) {
                message.addErrors(tempMessage.getErrors(), tempMessage.getStatus());
                if (MessageStatus.SC_SUCCESS.equals(tempMessage.getStatus())) {
                    message.appendPayload(tempMessage.getPayload());
                }
            }


//            message.setPayload((JsonNode) relationResponsePayload);

            reverseServiceStack.push(serviceRelation);
            commitServiceQueueQueue.add(serviceRelation);

        }

        return message.getPayload();
    }

    private CompositeServiceExecutionWrapper prepareServiceExecutionWrapper(ServiceRelation serviceRelation) {
        CompositeServiceExecutionWrapper serviceExecutionWrapper = serviceExecutionMap.get(serviceRelation.getId());
        if (null == serviceExecutionWrapper) {
            serviceExecutionWrapper = new CompositeServiceExecutionWrapper(serviceRelation);

            List<TransformerRelation> transformerRelations = transformerService.findAllTransformerRelationsBySource(
                    serviceRelation.getId());
            serviceExecutionWrapper.setTargetServiceRequestTransformers(
                    transformerRelations.stream()
                            .filter(t -> TransformerRelationType.SERVICE_RELATION_REQUEST.equals(t.getRelationType()))
                            .map(t -> new TransformerExecutionWrapper(t))
                            .collect(Collectors.toList()));
            serviceExecutionWrapper.setTargetServiceResponseTransformers(
                    transformerRelations.stream()
                            .filter(t -> TransformerRelationType.SERVICE_RELATION_RESPONSE.equals(t.getRelationType()))
                            .map(t -> new TransformerExecutionWrapper(t))
                            .collect(Collectors.toList()));
            serviceExecutionWrapper.setTargetServiceCommitRequestTransformers(
                    transformerRelations.stream()
                            .filter(t -> TransformerRelationType.SERVICE_RELATION_COMMIT_REQUEST.equals(t.getRelationType()))
                            .map(t -> new TransformerExecutionWrapper(t))
                            .collect(Collectors.toList()));
            serviceExecutionWrapper.setTargetServiceCommitResponseTransformers(
                    transformerRelations.stream()
                            .filter(t -> TransformerRelationType.SERVICE_RELATION_COMMIT_RESPONSE.equals(t.getRelationType()))
                            .map(t -> new TransformerExecutionWrapper(t))
                            .collect(Collectors.toList()));
            serviceExecutionWrapper.setTargetServiceReverseRequestTransformers(
                    transformerRelations.stream()
                            .filter(t -> TransformerRelationType.SERVICE_RELATION_REVERSE_REQUEST.equals(t.getRelationType()))
                            .map(t -> new TransformerExecutionWrapper(t))
                            .collect(Collectors.toList()));
            serviceExecutionWrapper.setTargetServiceReverseResponseTransformers(
                    transformerRelations.stream()
                            .filter(t -> TransformerRelationType.SERVICE_RELATION_REVERSE_RESPONSE.equals(t.getRelationType()))
                            .map(t -> new TransformerExecutionWrapper(t))
                            .collect(Collectors.toList()));

            serviceExecutionMap.put(serviceRelation.getId(), serviceExecutionWrapper);
        }
        return serviceExecutionWrapper;
    }

}
