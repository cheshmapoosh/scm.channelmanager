package ir.daneshrefah.scm.connector.api.component;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

public abstract class AbstractComponent extends DefaultComponent {

    @Override
    protected final Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return createInternalEndpoint(uri, remaining, parameters);
    }

    protected abstract AbstractEndpoint createInternalEndpoint(String uri, String remaining, Map<String, Object> parameters);

}
