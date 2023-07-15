package ir.daneshrefah.scm.nabconnectorimpl.component;

import ir.daneshrefah.scm.connectorapi.component.AbstractComponent;
import ir.daneshrefah.scm.connectorapi.component.AbstractEndpoint;
import ir.daneshrefah.scm.connectorapi.component.AbstractProducer;

public class NabEndpoint extends AbstractEndpoint {

    public NabEndpoint(String endpointUri, AbstractComponent component) {
        super(endpointUri, component);
    }

    @Override
    public AbstractProducer createInternalProducer() throws Exception {
        return new NabProducer(this);
    }

}
