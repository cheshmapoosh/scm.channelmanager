package ir.daneshrefah.scm.core.config;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2020-07-06
 */
public class CamelEventNotifierSupport extends EventNotifierSupport {

    private static final String PROPERTY_MESSAGE_INPUT = "PROPERTY_MESSAGE_INPUT";

    public CamelEventNotifierSupport() {
        setupIgnore(false);
//        setIgnoreRouteEvents(false);
    }

    @Override
    public void notify(CamelEvent event) throws Exception {
        if (event instanceof CamelEvent.RouteAddedEvent) {

        }
        if (event instanceof CamelEvent.ExchangeCreatedEvent) {
            Exchange exchange = ((CamelEvent.ExchangeCreatedEvent) event).getExchange();
            if (Objects.isNull(exchange.getProperty(PROPERTY_MESSAGE_INPUT))) {
//                String fromEndpoint = exchange.getFromEndpoint().getEndpointUri();
//                String routeId = exchange.getFromRouteId();
//                exchange.getContext().getRoute(routeId);

//                MessageInputContext.getCurrentContext()
            }
        }
    }

}
