package ir.daneshrefah.scm.provider.rest.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component("rest-provider")
public class RestProviderComponent extends DefaultComponent {
    public RestProviderComponent() {
    }

    public RestProviderComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        RestProviderEndpoint endpoint = new RestProviderEndpoint(uri, this, remaining);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
