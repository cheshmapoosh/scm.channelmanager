package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStep;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStepBuilder;
import org.apache.camel.model.TryDefinition;

import java.util.List;

public abstract class AbstractMultipleStepsExternalServiceProviderExecutor extends AbstractPreparedExternalServiceProviderExecutor{


    public AbstractMultipleStepsExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }



    protected abstract CamelInvocationStepBuilder call(TryDefinition routeDefinition);

    @Override
    protected List<CamelInvocationStep> callRoute(TryDefinition routeDefinition) {
        return call(routeDefinition).build();
    }




}
