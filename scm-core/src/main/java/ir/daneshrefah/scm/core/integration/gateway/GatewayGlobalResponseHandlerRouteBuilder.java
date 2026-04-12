package ir.daneshrefah.scm.core.integration.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.HttpClientErrorException;

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
                    return exchange.getIn().getBody() instanceof ScmFault;
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

    private FailResponse createScmFailResponse(Exchange exchange) {

        ScmFault scmResponse = (ScmFault) exchange.getIn().getBody();
        Error error = scmResponse.getErrors().getFirst();

        Integer code = extractCode(error.getErrorCode());

        Integer httpStatus = HttpStatusMapper.toHttpStatus(error.getStatus());
        if (httpStatus == null) {
            httpStatus = 500;
        }

        String detail = error.getMessageFa();
        String errorText = error.getException() != null
                ? error.getException().getMessage()
                : null;

        if (error.getException() instanceof HttpClientErrorException ex) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(ex.getResponseBodyAsString());

                httpStatus = root.path("status").asInt(httpStatus);
                detail = root.path("message").asText(detail);

            } catch (Exception ignored) {
            }
        }

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpStatus);

        return FailResponse.builder()
                .status(httpStatus)
                .code(code != null ? code : httpStatus)
                .title("error")
                .detail(detail)
                .error(errorText)
                .message(error.getMessage())
                .messageKey(error.getSource())
                .build();
    }

    private static Integer extractCode(String errorCode) {
        try {
            if (errorCode == null) {
                return 0;
            }
//            return Integer.parseInt(errorCode.replace("SCM-", ""));
            return Integer.parseInt(errorCode.contains("-") ? errorCode.split("-")[1] : errorCode);
        } catch (Exception e) {
            return null;
        }
    }


}
