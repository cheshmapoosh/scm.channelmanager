package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ServiceOperationEndpointResolver {

    public String resolve(String operationName) {
        if (StringUtils.contains(operationName, ':')) {
            return operationName;
        }
        return resolveRegisteredOperation(operationName);
    }

    /**
     * Resolves only the fixed route registered by the Operation layer.
     */
    public String resolveRegisteredOperation(String operationName) {
        return "direct:" + RouteIdSupport.operationRouteId(operationName);
    }
}
