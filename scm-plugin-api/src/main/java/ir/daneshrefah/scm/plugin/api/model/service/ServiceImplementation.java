package ir.daneshrefah.scm.plugin.api.model.service;

import org.apache.camel.model.RouteDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public interface ServiceImplementation {
    public RouteDefinition fullFill(RouteDefinition routeDefinition);

}
