package ir.daneshrefah.scm.connector.nab;


import ir.daneshrefah.scm.connector.api.component.AbstractComponent;
import ir.daneshrefah.scm.connector.api.component.AbstractEndpoint;
import ir.daneshrefah.scm.connector.api.component.AbstractProducer;

public class NabEndpoint extends AbstractEndpoint {

    public NabEndpoint(String endpointUri, AbstractComponent component) {
        super(endpointUri, component);
    }

    @Override
    public AbstractProducer createInternalProducer() throws Exception {
        return new NabProducer(this);
    }

}
