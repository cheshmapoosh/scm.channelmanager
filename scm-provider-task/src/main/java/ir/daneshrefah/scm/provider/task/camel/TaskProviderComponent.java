package ir.daneshrefah.scm.provider.task.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component(TaskProviderComponent.SCHEME)
public class TaskProviderComponent extends DefaultComponent {
    public static final String SCHEME = "scm-task";

    private final TaskProviderOperationAdapter operationAdapter;

    public TaskProviderComponent(TaskProviderOperationAdapter operationAdapter) {
        this.operationAdapter = operationAdapter;
    }

    public TaskProviderComponent(
            CamelContext camelContext,
            TaskProviderOperationAdapter operationAdapter
    ) {
        super(camelContext);
        this.operationAdapter = operationAdapter;
    }

    @Override
    protected Endpoint createEndpoint(
            String uri,
            String remaining,
            Map<String, Object> parameters
    ) throws Exception {
        String providerCode = remaining;
        String legacyOperationCode = null;
        if (operationAdapter.supportsOperation(remaining)) {
            providerCode = TaskProviderOperationAdapter.INTERNAL_PROVIDER_CODE;
            legacyOperationCode = remaining;
        } else {
            providerCode = operationAdapter.requireSupportedProvider(remaining);
        }
        TaskProviderEndpoint endpoint = new TaskProviderEndpoint(
                uri,
                this,
                providerCode,
                legacyOperationCode,
                operationAdapter
        );
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
