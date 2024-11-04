package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.ServiceCompositionType;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.RequiredArgsConstructor;
import org.apache.camel.model.ProcessorDefinition;
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
    protected void defineServiceRoute(ir.daneshrefah.scm.common.model.service.Service service, ProcessorDefinition<?> processorDefinition) {
        processorDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            JsonNode response = executeCompositeService(message);
            message.payload(response);
        });
    }

    protected JsonNode executeCompositeService(Message message) {
        CompositionService compositionService = (CompositionService) message.getHeader().getService();
        List<ServiceRelation> relations = getCompositionServiceRelation(compositionService);

        Deque<ServiceRelation> reverseServiceStack = new LinkedList<>();
        Queue<ServiceRelation> commitServiceQueueQueue = new ArrayDeque<>();
        JsonNode responsePayload = null;


        for (ServiceRelation serviceRelation : relations) {
            CompositeServiceExecutionWrapper serviceExecutionWrapper = prepareServiceExecutionWrapper(serviceRelation);

            JsonNode relationRequestPayload = null;
            try {
                relationRequestPayload = transformRequest(serviceExecutionWrapper.getTargetServiceRequestTransformers(), message);
            } catch (Exception e) {
                errorHandlerService.resolveMessageByException(message, e);
                break;
            }

            String terminalCode = MessageInputContext.getCurrentContext().getTerminalCode();
            String serviceCode = serviceRelation.getTargetService().getCode();
            Optional<TerminalServiceAccess> serviceAccess = terminalService
                    .findTerminalServiceAccessByTerminalCodeAndServiceCode(terminalCode, serviceCode);
            if (serviceAccess.isEmpty()) {
                throw new TerminalServiceNotFoundException(terminalCode, serviceCode);
            }
            Message tempMessage = MessageGenerator.getInstance().generateInternalMessage(serviceAccess.get().getService(), relationRequestPayload);
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

            responsePayload = transformResponse(serviceExecutionWrapper.getTargetServiceResponseTransformers(), message, tempMessage.getPayload());

            reverseServiceStack.push(serviceRelation);
            commitServiceQueueQueue.add(serviceRelation);

        }

        return Objects.nonNull(responsePayload) ? responsePayload : message.getPayload();
    }

    private List<ServiceRelation> getCompositionServiceRelation(CompositionService compositionService) {
        List<ServiceRelation> relations = compositionService.getRelations();
        if (null == relations) {
            relations = serviceService.findServiceRelationListBySourceServiceId(compositionService.getId());
            compositionService.setRelations(relations);
        }
        return relations;
    }

    private CompositeServiceExecutionWrapper prepareServiceExecutionWrapper(ServiceRelation serviceRelation) {
        CompositeServiceExecutionWrapper serviceExecutionWrapper = serviceExecutionMap.get(serviceRelation.getId());
        if (null == serviceExecutionWrapper) {
            serviceExecutionWrapper = new CompositeServiceExecutionWrapper(serviceRelation);

            List<TransformerRelation> transformerRelations = transformerService.findAllTransformerRelationsBySource(
                    serviceRelation.getSourceService().getId());
            serviceExecutionWrapper.setTargetServiceRequestTransformers(filterByRelationType(transformerRelations,TransformerRelationType.SERVICE_RELATION_REQUEST));
            serviceExecutionWrapper.setTargetServiceResponseTransformers(filterByRelationType(transformerRelations,TransformerRelationType.SERVICE_RELATION_RESPONSE));
            serviceExecutionWrapper.setTargetServiceCommitRequestTransformers(filterByRelationType(transformerRelations,TransformerRelationType.SERVICE_RELATION_COMMIT_REQUEST));
            serviceExecutionWrapper.setTargetServiceCommitResponseTransformers(filterByRelationType(transformerRelations,TransformerRelationType.SERVICE_RELATION_COMMIT_RESPONSE));
            serviceExecutionWrapper.setTargetServiceReverseRequestTransformers(filterByRelationType(transformerRelations,TransformerRelationType.SERVICE_RELATION_REVERSE_REQUEST));
            serviceExecutionWrapper.setTargetServiceReverseResponseTransformers(filterByRelationType(transformerRelations,TransformerRelationType.SERVICE_RELATION_REVERSE_RESPONSE));

            serviceExecutionMap.put(serviceRelation.getId(), serviceExecutionWrapper);
        }
        return serviceExecutionWrapper;
    }

    private List<TransformerExecutionWrapper> filterByRelationType( List<TransformerRelation> transformerRelations ,TransformerRelationType relationType){
       return transformerRelations.stream()
                .filter(t -> relationType.equals(t.getRelationType()))
                .map(TransformerExecutionWrapper::new)
                .collect(Collectors.toList());
    }
}
