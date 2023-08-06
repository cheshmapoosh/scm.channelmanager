package ir.daneshrefah.scm.plugin.api.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.MessageComponent;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.io.ClassLoader;
import org.apache.camel.model.RouteDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class DirectServiceImplementation extends ServiceImplementation {

    private List<ServiceComponentRelation> serviceComponentRelations;
    private Integer executionPolicy; // 1: all, 2: any

    public DirectServiceImplementation(Service service) {
        super(service);
    }

    @Override
    public RouteDefinition fullFill(RouteDefinition routeDefinition) {
        for (Iterator<ServiceComponentRelation> iterator = serviceComponentRelations.iterator(); iterator.hasNext(); ) {
            ServiceComponentRelation serviceComponentRelation = iterator.next();
            AbstractTransformer requestTransformer = TransformerType.JAVA.equals(serviceComponentRelation.getRequestTransformerType()) ?
                    ClassLoader.createInstanceOfClass(serviceComponentRelation.getRequestTransformerClass(), AbstractTransformer.class) :
                    null;
            AbstractTransformer responseTransformer = TransformerType.JAVA.equals(serviceComponentRelation.getResponseTransformerType()) ?
                    ClassLoader.createInstanceOfClass(serviceComponentRelation.getResponseTransformerClass(), AbstractTransformer.class):
                    null;
            routeDefinition = routeDefinition.process(exchange -> {
                Message message = exchange.getMessage().getBody(Message.class);
                MessageComponent component = new MessageComponent();
                component.setServiceComponent(serviceComponentRelation.getServiceComponent());
                message.setMessageComponent(component);
                Object request = null;
                if (null != requestTransformer) {
                    request = requestTransformer.transform(message,
                            serviceComponentRelation.getRequestMetadata());
                } else {
                    request = message.getPayload().toString();
                }
                component.setPayload(request);
            });

            String targetUri = "direct:SVC_" + serviceComponentRelation.getServiceComponent().getServiceComponentProvider().getCode() +
                                "_" +
                    serviceComponentRelation.getServiceComponent().getCode();
            routeDefinition = routeDefinition.to(targetUri);

            routeDefinition = routeDefinition.process(exchange -> {
                Message message = exchange.getMessage().getBody(Message.class);
                JsonNode response = null;
                if (null != responseTransformer) {
                    response = (JsonNode) responseTransformer.transform(message,
                            serviceComponentRelation.getRequestMetadata());
                } else {
                    response = message.getPayload();
                }
                if (null != response)
                    message.setPayload(response);
            });
        }
        routeDefinition = routeDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            if (Status.SC_PROCESSING.equals(message.getStatus())) {
                message.setStatus(Status.SC_SUCCESS);
            }
        });
        return routeDefinition;
    }

    public List<ServiceComponentRelation> getServiceComponentRelations() {
        return serviceComponentRelations;
    }

    public void setServiceComponentRelations(List<ServiceComponentRelation> serviceComponentRelations) {
        this.serviceComponentRelations = serviceComponentRelations;
    }

    public Integer getExecutionPolicy() {
        return executionPolicy;
    }

    public void setExecutionPolicy(Integer executionPolicy) {
        this.executionPolicy = executionPolicy;
    }

    public void addServiceComponentRelation(ServiceComponentRelation serviceComponentRelation) {
        if (null == serviceComponentRelations) {
            serviceComponentRelations = new ArrayList<>();
        }
        serviceComponentRelations.add(serviceComponentRelation);
    }
}
