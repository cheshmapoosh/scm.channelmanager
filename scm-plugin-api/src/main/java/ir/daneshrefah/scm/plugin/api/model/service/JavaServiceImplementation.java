package ir.daneshrefah.scm.plugin.api.model.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.apache.camel.model.RouteDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-30
 */
public class JavaServiceImplementation extends ServiceImplementation {

    private AbstractJavaService javaService;

    public JavaServiceImplementation(Service service, AbstractJavaService javaService) {
        super(service);
        this.javaService = javaService;
    }

    @Override
    public RouteDefinition fullFill(RouteDefinition routeDefinition) {
        routeDefinition.process(exchange -> {
            javaService.execute(exchange.getMessage().getBody(Message.class));
        });
        return routeDefinition;
    }

}
