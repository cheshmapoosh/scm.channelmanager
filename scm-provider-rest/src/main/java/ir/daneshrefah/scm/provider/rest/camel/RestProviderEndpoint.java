package ir.daneshrefah.scm.provider.rest.camel;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.Category;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "scm-rest",
        title = "REST Provider",
        syntax = "scm-rest:providerCode",
        producerOnly = true,
        category = {Category.HTTP}
)
@Getter
@Setter
public class RestProviderEndpoint extends DefaultEndpoint {
    private final String remaining;

    @UriParam
    private String provider;

    @UriParam
    private Integer timeoutMs;

    @UriParam
    private String method;

    @UriParam
    private Boolean rateLimitEnabled;

    @UriParam
    private String rateLimitBucket;

    @UriParam
    private String rateLimitKey;

    public RestProviderEndpoint(String endpointUri, Component component, String remaining) {
        super(endpointUri, component);
        this.remaining = remaining;
    }

    @Override
    public Producer createProducer() {
        return new RestProviderProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException("REST provider endpoint is producer-only");
    }
}
