package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

/**
 * ATPS Component for Camel integration.
 * This component is used to create ATPS endpoints and bind properties.
 */
@Component("atps")
public class AtpsComponent extends DefaultComponent {

    public AtpsComponent() {
        super();
    }

    public AtpsComponent(CamelContext context) {
        super(context);
    }

    /**
     * Creates an endpoint for ATPS protocol.
     *
     * @param uri        The full URI of the endpoint
     * @param remaining  The remaining part of the URI (e.g., "tcp://host:port")
     * @param parameters A map of parameters to configure the endpoint
     * @return The created AtpsEndpoint
     * @throws Exception If an error occurs during endpoint creation
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        var nettyBaseUri = "netty:" + remaining;  // Construct the netty URI

        // Create the AtpsEndpoint and bind URI parameters
        var endpoint = new AtpsEndpoint(uri, this, nettyBaseUri);
        setProperties(endpoint, parameters);

        return endpoint;
    }
}
