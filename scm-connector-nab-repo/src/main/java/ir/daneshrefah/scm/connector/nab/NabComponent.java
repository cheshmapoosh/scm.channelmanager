package ir.daneshrefah.scm.connector.nab;


import ir.daneshrefah.scm.connector.api.component.AbstractComponent;
import ir.daneshrefah.scm.connector.api.component.AbstractEndpoint;

import java.util.Map;

public class NabComponent extends AbstractComponent {
    @Override
    protected AbstractEndpoint createInternalEndpoint(String uri, String remaining, Map<String, Object> parameters) {
        AbstractEndpoint endpoint = new NabEndpoint(uri, this);
        return endpoint;
    }
}
