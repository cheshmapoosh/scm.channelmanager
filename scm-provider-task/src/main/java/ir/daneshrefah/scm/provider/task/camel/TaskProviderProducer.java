package ir.daneshrefah.scm.provider.task.camel;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

public class TaskProviderProducer extends DefaultProducer {
    private final String providerCode;
    private final String legacyOperationCode;
    private final TaskProviderOperationAdapter operationAdapter;

    public TaskProviderProducer(
            TaskProviderEndpoint endpoint,
            String providerCode,
            String legacyOperationCode,
            TaskProviderOperationAdapter operationAdapter
    ) {
        super(endpoint);
        this.providerCode = providerCode;
        this.legacyOperationCode = legacyOperationCode;
        this.operationAdapter = operationAdapter;
    }

    @Override
    public void process(Exchange exchange) {
        operationAdapter.execute(providerCode, legacyOperationCode, exchange);
    }
}
