package ir.daneshrefah.scm.nabconnectorimpl.component;

import ir.daneshrefah.scm.common.model.Constants;
import ir.daneshrefah.scm.common.model.GatewayOperation;
import ir.daneshrefah.scm.connectorapi.component.AbstractEndpoint;
import ir.daneshrefah.scm.connectorapi.component.AbstractProducer;
import ir.daneshrefah.scm.connectorapi.component.ScmExchange;

public class NabProducer extends AbstractProducer {

    public NabProducer(AbstractEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public Object internalProcess(ScmExchange exchange) throws Exception {
        GatewayOperation gatewayOperation = (GatewayOperation) exchange.getHeaders().get(Constants.MESSAGE_HEADER_KEY_GATEWAY_OPERATION);
        Object body = exchange.getBody();
        return "Nab Hello 2";
    }

}
