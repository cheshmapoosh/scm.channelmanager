package ir.daneshrefah.scm.provider.scm.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceRegistry;
import org.apache.camel.Category;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = ScmComponent.SCHEME,
        title = "Internal SCM Resource Provider",
        syntax = "scm:resourceName",
        producerOnly = true,
        category = {Category.CORE}
)
public final class ScmEndpoint extends DefaultEndpoint {

    private final String resourceName;
    private final ScmResourceRegistry resourceRegistry;
    private final ObjectMapper objectMapper;

    public ScmEndpoint(
            String endpointUri,
            Component component,
            String resourceName,
            ScmResourceRegistry resourceRegistry,
            ObjectMapper objectMapper
    ) {
        super(endpointUri, component);
        this.resourceName = resourceName;
        this.resourceRegistry = resourceRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public Producer createProducer() {
        return new ScmProducer(this, resourceName, resourceRegistry, objectMapper);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException("The scm endpoint is producer-only");
    }
}
