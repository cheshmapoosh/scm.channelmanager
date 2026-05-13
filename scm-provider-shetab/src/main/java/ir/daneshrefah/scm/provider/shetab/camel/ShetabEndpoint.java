package ir.daneshrefah.scm.provider.shetab.camel;

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
        scheme = "shetab",
        title = "Shetab",
        syntax = "shetab:request",
        producerOnly = true,
        category = {Category.NETWORKING}
)
@Getter
@Setter
public class ShetabEndpoint extends DefaultEndpoint {
    private final String remaining;

    @UriParam
    private String provider;

    @UriParam
    private Integer timeoutMs;

    @UriParam
    private Boolean rateLimitEnabled;

    @UriParam
    private String rateLimitBucket;

    @UriParam
    private String rateLimitKey;

    public ShetabEndpoint(String endpointUri, Component component, String remaining) {
        super(endpointUri, component);
        this.remaining = remaining;
    }

    @Override
    public Producer createProducer() {
        return new ShetabProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException("Shetab endpoint is producer-only");
    }
}
