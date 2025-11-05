package ir.daneshrefah.scm.core.integration;

import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsComponent;
import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsResponseBodyDecoder;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Exception {
        CamelContext ctx = new DefaultCamelContext();
        try {

            ctx.addComponent("atps", new AtpsComponent(ctx));
            ctx.getRegistry().bind("atpsResponseBodyDecoder", new AtpsResponseBodyDecoder(99999));
            RouteBuilder routeBuilder = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:to-core")
                            .to("atps:tcp://10.15.27.12:3080"
                                    + "?connectTimeout=3000"
                                    + "&requestTimeout=5000"
                                    + "&validateAck=true"
                                    + "&ackEquals=00000")
                            .log("Core replied with ${bodyAs(byte[]).length} bytes");
                }
            };
            ctx.addRoutes(routeBuilder);
            ctx.start();
            ProducerTemplate tpl = ctx.createProducerTemplate();
            String payload = "970514040806181345999998    12345678909321584523527230                                  ";
            String[] reply = tpl.requestBody("direct:to-core", payload, String[].class);
            System.out.println("Reply : " + Arrays.toString(reply));
        } finally {
            ctx.stop();
        }
        // keep alive or stop as you wish
    }

}
