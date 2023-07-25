package ir.daneshrefah.scm.plugin.api.model.service;

import org.apache.camel.model.RouteDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public abstract class ServiceImplementation {

    protected Service service;

    public ServiceImplementation(Service service) {
        this.service = service;
    }

    public abstract RouteDefinition fullFill(RouteDefinition routeDefinition);

}
