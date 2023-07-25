package ir.daneshrefah.scm.plugin.api.model.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.MessageComponent;
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
            AbstractTransformer requestTransformer = ClassLoader.createInstanceOfClass(
                    serviceComponentRelation.getRequestTransformerClass(), AbstractTransformer.class);
            AbstractTransformer responseTransformer = ClassLoader.createInstanceOfClass(
                    serviceComponentRelation.getResponseTransformerClass(), AbstractTransformer.class);
            AbstractTransformer finalRequestTransformer = requestTransformer;
            routeDefinition = routeDefinition.process(exchange -> {
                Message message = exchange.getMessage().getBody(Message.class);
                MessageComponent component = new MessageComponent();
                component.setServiceComponent(serviceComponentRelation.getServiceComponent());
                message.setMessageComponent(component);
                if (null != requestTransformer) {
                    Object request = finalRequestTransformer.transform(service.getRequestJSONSchema(),
                            serviceComponentRelation.getServiceComponent().getRequestJSONSchema(), message,
                            serviceComponentRelation.getRequestMetadata());
                    component.setPayload(request);
                }
            });

            String targetUri = "direct:SVC_" + serviceComponentRelation.getServiceComponent().getServiceComponentProvider().getCode() +
                                "_" +
                    serviceComponentRelation.getServiceComponent().getCode();
            routeDefinition = routeDefinition.to(targetUri);
        }
//        routeDefinition = routeDefinition.end();
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
