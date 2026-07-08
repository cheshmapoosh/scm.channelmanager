package ir.daneshrefah.scm.provider.task.camel;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

public class TaskProviderProducer extends DefaultProducer {
    private final String providerCode;
    private final TaskProviderOperationAdapter operationAdapter;

    public TaskProviderProducer(
            TaskProviderEndpoint endpoint,
            String providerCode,
            TaskProviderOperationAdapter operationAdapter
    ) {
        super(endpoint);
        this.providerCode = providerCode;
        this.operationAdapter = operationAdapter;
    }

    @Override
    public void process(Exchange exchange) {
        operationAdapter.execute(providerCode, exchange);
    }
}
