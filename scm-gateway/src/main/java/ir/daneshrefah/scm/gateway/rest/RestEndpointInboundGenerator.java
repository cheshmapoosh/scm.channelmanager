package ir.daneshrefah.scm.gateway.rest;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestEndpointInboundGenerator extends RouteBuilder {

//    @Autowired
//    private ApplicationSetting applicationSetting;

    @Override
    public void configure() throws Exception {
        /*from("netty-http:http://0.0.0.0:8080/api/cities1")
                .choice()
                    .when(header("CamelHttpMethod").isEqualTo("GET"))
                        .to("direct:cities1")
                    .when(header("CamelHttpMethod").isEqualTo("POST"))
                        .to("direct:cities4");

        from("direct:cities1")
                .setBody().constant("cities1");

        from("netty-http:http://0.0.0.0:8080/api/cities2")
                .choice()
                    .when(header("CamelHttpMethod").isEqualTo("POST"))
                        .to("direct:cities2");

        from("direct:cities2")
                .setBody().constant("cities2");

        from("netty-http:http://0.0.0.0:8081/api/cities3")
                .choice()
                    .when(header("CamelHttpMethod").isEqualTo("GET"))
                        .to("direct:cities3");

        from("direct:cities3")
                .setBody().constant("cities3");

        from("direct:cities4")
                .setBody().constant("cities4");*/
    }

}
