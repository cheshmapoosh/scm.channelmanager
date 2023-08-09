package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

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

    @Override
    protected Object executeInternal(ir.daneshrefah.scm.plugin.api.model.service.Service service, Message message, Object requestPayload) {
        CompositionService compositionService = (CompositionService) service;
        List<ServiceRelation> relations = compositionService.getRelations();
        for (Iterator<ServiceRelation> iterator = relations.iterator(); iterator.hasNext(); ) {
            ServiceRelation serviceRelation = iterator.next();
            serviceRelation.getSourceService();
//            JsonNode response = serviceProducerTemplate.callService(serviceRelation.getTargetService(), message);
//            message.appendResponse(response, responseKey);
        }
        return null;
    }
}
