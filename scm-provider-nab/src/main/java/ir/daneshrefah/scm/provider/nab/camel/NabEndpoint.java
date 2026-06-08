package ir.daneshrefah.scm.provider.nab.camel;

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
        scheme = "scm-nab",
        title = "NAB",
        syntax = "scm-nab:providerCode",
        producerOnly = true,
        category = {Category.NETWORKING}
)
@Getter
@Setter
public class NabEndpoint extends DefaultEndpoint {
    private final String remaining;

    @UriParam
    private String provider;

    @UriParam
    private Integer timeoutMs;

    @UriParam
    private String charset;

    @UriParam
    private Boolean rateLimitEnabled;

    @UriParam
    private String rateLimitBucket;

    @UriParam
    private String rateLimitKey;

    public NabEndpoint(String endpointUri, Component component, String remaining) {
        super(endpointUri, component);
        this.remaining = remaining;
    }

    @Override
    public Producer createProducer() {
        return new NabProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException("NAB endpoint is producer-only");
    }
}
