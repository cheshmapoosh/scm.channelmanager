package ir.daneshrefah.scm.provider.nab.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component("nab")
public class NabComponent extends DefaultComponent {
    public NabComponent() {
    }

    public NabComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        NabEndpoint endpoint = new NabEndpoint(uri, this, remaining);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
