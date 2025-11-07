package nabtcp;


import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsComponent;
import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsHelper;
import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsResponseBodyDecoder;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataType;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@CamelSpringBootTest
@SpringBootTest(classes = ir.daneshrefah.scm.plugin.camel.CamelWebclientApplication.class)
public class NabTcpClientTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private AtpsComponent nabTcpClientComponent;

    @Test
    void testSendHeaderAndBody() throws Exception {
        String s = """
                {
                  "command": "97",
                  "service": "05",
                  "dateTime": "14040806181345",
                  "cmUserId": "999998",
                  "cmPassword": "1234567890",
                  "rquid": "9321584523527230"
                }
                """;

        camelContext.addComponent("atps",nabTcpClientComponent);

        camelContext.getRegistry().bind("atpsResponseBodyDecoder", new AtpsResponseBodyDecoder(99999));
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:to-core")
//                        .transform().groovy("""
//                       import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsHelper
//                       return AtpsHelper.jsonToPlainText(body)
//                       """)
                        .to("atps:tcp://10.15.27.12:3080"
                                + "?connectTimeout=3000"
                                + "&requestTimeout=5000"
                                + "&validateAck=true"
                                + "&ackEquals=00000")

                        .log("Core replied with ${bodyAs(String[]).length} bytes");
            }
        };
        camelContext.addRoutes(routeBuilder);
        camelContext.start();
        ProducerTemplate tpl = camelContext.createProducerTemplate();


        String payload = "";

        String[] reply = tpl.requestBody("direct:to-core", payload, String[].class);
        ObjectNode[] nodes = AtpsHelper.parseFixedWidthArray(reply);
        System.out.println("Reply : " + Arrays.toString(reply));


    }
}
