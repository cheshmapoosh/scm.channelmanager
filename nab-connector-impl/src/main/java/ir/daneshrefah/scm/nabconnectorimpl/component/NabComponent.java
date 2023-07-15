package ir.daneshrefah.scm.nabconnectorimpl.component;

import ir.daneshrefah.scm.connectorapi.component.AbstractComponent;
import ir.daneshrefah.scm.connectorapi.component.AbstractEndpoint;

import java.util.Map;

public class NabComponent extends AbstractComponent {
    @Override
    protected AbstractEndpoint createInternalEndpoint(String uri, String remaining, Map<String, Object> parameters) {
        AbstractEndpoint endpoint = new NabEndpoint(uri, this);
        return endpoint;
    }
}
