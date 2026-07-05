package ir.daneshrefah.scm.web.observation.propagation;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ScmCamelTracePropagationRouteBuilder extends RouteBuilder {
    private final ScmCamelTracePropagationProcessor processor;

    public ScmCamelTracePropagationRouteBuilder(ScmCamelTracePropagationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void configure() {
        interceptSendToEndpoint("http://*").process(processor);
        interceptSendToEndpoint("https://*").process(processor);
        interceptSendToEndpoint("http4://*").process(processor);
        interceptSendToEndpoint("https4://*").process(processor);
    }
}
