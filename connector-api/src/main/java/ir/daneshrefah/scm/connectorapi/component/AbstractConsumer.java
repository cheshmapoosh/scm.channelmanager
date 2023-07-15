package ir.daneshrefah.scm.connectorapi.component;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.support.DefaultProducer;

public abstract class AbstractConsumer extends DefaultConsumer {


    public AbstractConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }
}
