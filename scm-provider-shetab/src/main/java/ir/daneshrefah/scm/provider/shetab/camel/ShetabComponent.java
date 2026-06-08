package ir.daneshrefah.scm.provider.shetab.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component("scm-shetab")
public class ShetabComponent extends DefaultComponent {
    public ShetabComponent() {
    }

    public ShetabComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        ShetabEndpoint endpoint = new ShetabEndpoint(uri, this, remaining);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
