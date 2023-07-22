package ir.daneshrefah.scm.core.inbound;

import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public class RestInboundChannelGenerator extends AbstractInboundChannelGenerator {

    @Override
    public void configure() throws Exception {
        restConfiguration().host("localhost").port(8090).bindingMode(RestBindingMode.json);
        from("rest:post:api/test")
                .log("body ${body}")
                .to("direct:SERVICE_ACH_XFER")
                .end();

//        from("direct:test")
//                .setBody().constant("Helloooooo2")
//                .end();
    }

}
