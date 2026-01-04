package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.model.FailResponse;
import ir.daneshrefah.scm.common.model.ScmResponse;
import ir.daneshrefah.scm.common.model.error.Error;
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
//                .process(exchange -> exchange.getIn().setBody(createScmResponse(exchange)))
                .choice()
                .when(exchange -> {
                   return  exchange.getIn().getBody() instanceof ScmFault;
//                    ScmResponse response = (ScmResponse) (exchange.getIn().getBody());
//                    return response.getErrors() != null && !response.getErrors().isEmpty();
                })
                .process(exchange -> exchange.getIn().setBody(createScmFailResponse(exchange)))
                .otherwise()
                .process(exchange -> exchange.getIn().setBody(createScmResponse(exchange)))
                .end()
                .marshal()
                .json(JsonLibrary.Jackson)
                .end()
                .choice()
                .when(exchangeProperty(Message.GATEWAY_CHANNEL_PROTOCOL)
                        .isNotEqualTo(ProtocolType.REST))
                .throwException(
                        new IllegalStateException("Unsupported protocol type"))
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

    private FailResponse createScmFailResponse(Exchange exchange){

        ScmFault scmResponse = (ScmFault)  exchange.getIn().getBody();
        Error error = scmResponse.getErrors().get(0);
        int code = extractCode(error.getErrorCode());

        Integer httpStatus = HttpStatusMapper.toHttpStatus(error.getStatus());
        if(httpStatus == null)
            httpStatus = 500;
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpStatus);

        return FailResponse
                .builder()
                .status(500)
                .code(code)
                .title("error")
                .detail(error.getMessageFa())
                .error(error.getException().getMessage())
                .message(error.getMessage())
                .messageKey(error.getSource())
                .build();
    }

    private static Integer extractCode(String errorCode) {
        try {
            return Integer.parseInt(errorCode.replace("SCM-", ""));
        } catch (Exception e) {
            return null;
        }
    }

//    private Object createScmSuccessResponse(Exchange exchange){
//        return null;
//    }
}
