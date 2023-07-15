package ir.daneshrefah.scm.gateway.camel;

import ir.daneshrefah.scm.common.model.Constants;
import ir.daneshrefah.scm.common.model.GatewayOperation;
import ir.daneshrefah.scm.gateway.service.GatewayOperationService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RestEndpointInboundGenerator extends RouteBuilder {

    @Value("scm.inbound.host")
    private String inboundHost;
//    @Value("scm.inbound.port")
//    private String inboundPort;
    @Autowired
    private GatewayOperationService operationInfoService;

    @Override
    public void configure() throws Exception {
        restConfiguration().host("localhost").port(8888).bindingMode(RestBindingMode.json);
        Map<String, GatewayOperation> gatewayOperationMap = operationInfoService.findAllGatewayOperation();
        for (Map.Entry<String, GatewayOperation> entry : gatewayOperationMap.entrySet()) {
            GatewayOperation operation = entry.getValue();
//            from("rest:get:api" + operation.getUrlBase())
//                    .routeId(operation.getName())
//                    .setBody().constant("Helloooooo")
//                    .end();
            from("rest:post:api" + operation.getUrlBase())
//                    TODO 0) log input
//                    TODO 1) validate by input json schema **
//                    TODO 2) inject default properties
//                    TODO 3) parse JSONObject and inject to properties **
//                    TODO 4) check rate limit
//                    .onException(Exception.class)
//                    .handled(true)
//                    .to("seda:restError")
//                    .end()
                    .setHeader(Constants.MESSAGE_HEADER_KEY_GATEWAY_OPERATION)
                    .constant(operation)
//                    .bean(DynamicValidator.class, "validate(${body.input})")
//                    .bean(DynamicValidator.class, "validate")
//                    .process(new ValidateInputSchema(operation))
                    .log("body ${body}")

                    .to("direct:" + operation.getCode())

                    .end()
            ;
        }
    }

}
