package ir.daneshrefah.scm.core.integration.operation.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationProvider;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderOperationTypeHandlerTest {

    private final ProviderOperationTypeHandler handler = new ProviderOperationTypeHandler(new ObjectMapper());

    @Test
    void providerOperationAlwaysTargetsProviderUri() {
        Operation operation = operation(provider("hps", "shetab:request?provider=hps", true));
        operation.setPath("direct:wrong-target");

        RouteDefinition route = new RouteDefinition();
        handler.config(route, operation);

        ToDefinition target = (ToDefinition) route.getOutputs().get(1);
        assertEquals("shetab:request?provider=hps", target.getEndpointUri());
    }

    @Test
    void providerOperationRequiresActiveProviderInstance() {
        Operation operation = operation(provider("hps", "shetab:request?provider=hps", false));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.config(new RouteDefinition(), operation));

        assertEquals("Provider operation is bound to an inactive provider: CARD_INQUIRY", exception.getMessage());
    }

    @Test
    void providerOperationRequiresCamelProviderUri() {
        Operation operation = operation(provider("hps", "hps", true));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.config(new RouteDefinition(), operation));

        assertEquals("Provider operation target URI must include a Camel scheme for operation CARD_INQUIRY", exception.getMessage());
    }

    private Operation operation(OperationProvider provider) {
        Operation operation = new Operation();
        operation.setName("CARD_INQUIRY");
        operation.setProvider(provider);
        return operation;
    }

    private OperationProvider provider(String name, String uri, boolean active) {
        OperationProvider provider = new OperationProvider();
        provider.setName(name);
        provider.setUri(uri);
        provider.setActive(active);
        return provider;
    }
}
