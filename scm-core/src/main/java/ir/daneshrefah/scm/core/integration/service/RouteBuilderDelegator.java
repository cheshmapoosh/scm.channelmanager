package ir.daneshrefah.scm.core.integration.service;

import org.apache.camel.model.RouteDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-15
 */
public interface RouteBuilderDelegator {

    RouteDefinition from(String uri);

}
