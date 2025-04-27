package ir.daneshrefah.scm.plugin.camel.component.webclient;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component("webclient")
public class WebClientComponent extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        WebClientEndpoint endpoint = new WebClientEndpoint(uri, this);
        setProperties(endpoint, parameters); // Bind @UriParam fields
        endpoint.setRawUri(remaining);
        return endpoint;
    }
}