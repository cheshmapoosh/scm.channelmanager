package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.model.ScmResponse;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class GatewayGlobalResponseHandlerRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from(Routes.GLOBAL_RESPONSE_HANDLER)
                .choice()
                .when(exchange -> exchange.getProperty(Message.GATEWAY_CHANNEL_PROTOCOL) == ProtocolType.REST)
                .process(exchange -> exchange.getIn().setBody(createScmResponse(exchange)))
                .marshal()
                .json(JsonLibrary.Jackson)
                .endChoice()
                .otherwise()
                .throwException(new IllegalStateException("Unsupported protocol type"))
                .end();

    }
    
    private ScmResponse createScmResponse(Exchange exchange) {
        ScmResponse response;
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        TraceUtils.getInstance().traceScmResponse(exchange, service);
        if (exchange.getIn().getBody() instanceof ScmFault scmFault) {
            response = ScmResponse
                    .builder()
                    .status(scmFault.getStatus())
                    .result(null)
                    .errors(scmFault.getErrors())
                    .build();
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatusMapper.toHttpStatus(response.getStatus()));
        } else {
            response = ScmResponse
                    .builder()
                    .status(MessageStatus.SC_SUCCESS)
                    .result(exchange.getIn().getBody())
                    .errors(null)
                    .build();
        }
        return response;
    }

}
