package ir.daneshrefah.scm.provider.task.camel;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

public class TaskProviderProducer extends DefaultProducer {
    private final String operationCode;
    private final TaskProviderOperationAdapter operationAdapter;

    public TaskProviderProducer(
            TaskProviderEndpoint endpoint,
            String operationCode,
            TaskProviderOperationAdapter operationAdapter
    ) {
        super(endpoint);
        this.operationCode = operationCode;
        this.operationAdapter = operationAdapter;
    }

    @Override
    public void process(Exchange exchange) {
        operationAdapter.execute(operationCode, exchange);
    }
}
