package ir.daneshrefah.scm.provider.scm.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceNames;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component(ScmComponent.SCHEME)
public final class ScmComponent extends DefaultComponent {

    public static final String SCHEME = "scm";

    private final ScmResourceRegistry resourceRegistry;
    private final ObjectMapper objectMapper;

    public ScmComponent(ScmResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
        this.resourceRegistry = resourceRegistry;
        this.objectMapper = objectMapper;
    }

    public ScmComponent(
            CamelContext camelContext,
            ScmResourceRegistry resourceRegistry,
            ObjectMapper objectMapper
    ) {
        super(camelContext);
        this.resourceRegistry = resourceRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Endpoint createEndpoint(
            String uri,
            String remaining,
            Map<String, Object> parameters
    ) {
        if (!parameters.isEmpty()) {
            throw new IllegalArgumentException("The scm component does not accept endpoint parameters");
        }
        String resourceName = ScmResourceNames.requireResourceName(
                remaining,
                "SCM endpoint Resource name"
        );
        resourceRegistry.requireResource(resourceName);
        return new ScmEndpoint(uri, this, resourceName, resourceRegistry, objectMapper);
    }
}
