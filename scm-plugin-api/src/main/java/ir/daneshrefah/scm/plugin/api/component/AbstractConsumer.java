package ir.daneshrefah.scm.plugin.api.component;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;

public abstract class AbstractConsumer extends DefaultConsumer {


    public AbstractConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }
}
