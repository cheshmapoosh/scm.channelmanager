package ir.daneshrefah.scm.connectorapi.component;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;

public abstract class AbstractEndpoint extends DefaultEndpoint {

    public AbstractEndpoint(String endpointUri, AbstractComponent component) {
        super(endpointUri, component);
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public final Producer createProducer() throws Exception {
        return createInternalProducer();
    }

    @Override
    public final Consumer createConsumer(Processor processor) throws Exception {
        return null;
    }

    public abstract AbstractProducer createInternalProducer() throws Exception;

    public AbstractConsumer createInternalProducer(Processor processor) throws Exception {
        return null;
    };

}
