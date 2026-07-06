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
        syntax = "scm-task:operationCode",
        producerOnly = true,
        category = {Category.CORE}
)
public class TaskProviderEndpoint extends DefaultEndpoint {
    private final String operationCode;
    private final TaskProviderOperationAdapter operationAdapter;

    public TaskProviderEndpoint(
            String endpointUri,
            Component component,
            String operationCode,
            TaskProviderOperationAdapter operationAdapter
    ) {
        super(endpointUri, component);
        this.operationCode = operationCode;
        this.operationAdapter = operationAdapter;
    }

    @Override
    public Producer createProducer() {
        return new TaskProviderProducer(this, operationCode, operationAdapter);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException(
                TaskProviderComponent.SCHEME + " endpoint is producer-only");
    }
}
