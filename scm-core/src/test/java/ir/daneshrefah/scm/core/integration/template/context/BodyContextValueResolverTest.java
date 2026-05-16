package ir.daneshrefah.scm.core.integration.template.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BodyContextValueResolverTest {

    private final BodyContextValueResolver resolver = new BodyContextValueResolver(new ObjectMapper());

    @Test
    void resolvesNestedJsonValuesWithoutSharedState() {
        DefaultExchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getIn().setBody("""
                {
                  "fundTransfer": {
                    "amount": 1,
                    "destinationCardNumber": "5047061044402697"
                  },
                  "trk2EquivData": {
                    "cardExpirationYearMonth": "0412"
                  }
                }
                """);

        assertTrue(resolver.supports("body.fundTransfer.amount"));
        assertEquals(1, resolver.resolve("body.fundTransfer.amount", exchange));
        assertEquals("5047061044402697", resolver.resolve("body.fundTransfer.destinationCardNumber", exchange));
        assertEquals("0412", resolver.resolve("body.trk2EquivData.cardExpirationYearMonth", exchange));
        assertNull(resolver.resolve("body.fundTransfer.unknown", exchange));
    }

    @Test
    void blankBodyReturnsNullForMissingValue() {
        DefaultExchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getIn().setBody("");

        assertNull(resolver.resolve("body.fundTransfer.amount", exchange));
    }
}
