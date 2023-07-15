package ir.daneshrefah.scm.gateway.camel;

import ir.daneshrefah.scm.common.model.GatewayOperation;
import ir.daneshrefah.scm.gateway.service.GatewayOperationService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServicePoolInboundGenerator extends RouteBuilder {

    @Autowired
    private GatewayOperationService operationInfoService;

    @Override
    public void configure() throws Exception {
        Map<String, GatewayOperation> gatewayOperationMap = operationInfoService.findAllGatewayOperation();
        for (Map.Entry<String, GatewayOperation> entry : gatewayOperationMap.entrySet()) {
            GatewayOperation operation = entry.getValue();
            from("direct:"  + operation.getCode())
                    .log("receive nab provider")
//                    .setBody().constant("Helloooooo2")
//                    .bean(nabRequestTransformer)
//                    .to("log:request?showAll=true")
//                    .log("body is : ${body}")
//                    .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
                    .to(operation.getServiceProviderComponent() + ":" + operation.getCode())
//                    .to("log:response?showAll=true")
//                    .bean(nabResponseTransformer)
                    .end();

        }

    }
}
