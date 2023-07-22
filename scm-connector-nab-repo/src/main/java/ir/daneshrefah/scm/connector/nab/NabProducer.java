package ir.daneshrefah.scm.connector.nab;


import ir.daneshrefah.scm.connector.api.component.AbstractEndpoint;
import ir.daneshrefah.scm.connector.api.component.AbstractProducer;
import ir.daneshrefah.scm.connector.api.component.ScmExchange;

public class NabProducer extends AbstractProducer {

    public NabProducer(AbstractEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public Object internalProcess(ScmExchange exchange) throws Exception {
//        GatewayOperation gatewayOperation = (GatewayOperation) exchange.getHeaders().get(Constants.MESSAGE_HEADER_KEY_GATEWAY_OPERATION);
//        Object body = exchange.getBody();
        return "Nab Hello 3";
    }

}
