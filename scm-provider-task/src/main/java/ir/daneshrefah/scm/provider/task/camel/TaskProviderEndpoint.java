package ir.daneshrefah.scm.provider.task.camel;

import org.apache.camel.Category;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = TaskProviderComponent.SCHEME,
        title = "SCM Task Provider",
        syntax = "scm-task:providerCode",
        producerOnly = true,
        category = {Category.CORE}
)
public class TaskProviderEndpoint extends DefaultEndpoint {
    private final String providerCode;
    private final String legacyOperationCode;
    private final TaskProviderOperationAdapter operationAdapter;

    public TaskProviderEndpoint(
            String endpointUri,
            Component component,
            String providerCode,
            String legacyOperationCode,
            TaskProviderOperationAdapter operationAdapter
    ) {
        super(endpointUri, component);
        this.providerCode = providerCode;
        this.legacyOperationCode = legacyOperationCode;
        this.operationAdapter = operationAdapter;
    }

    @Override
    public Producer createProducer() {
        return new TaskProviderProducer(
                this,
                providerCode,
                legacyOperationCode,
                operationAdapter
        );
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException(
                TaskProviderComponent.SCHEME + " endpoint is producer-only");
    }
}
