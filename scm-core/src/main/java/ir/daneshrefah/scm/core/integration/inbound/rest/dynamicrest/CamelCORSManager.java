package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import org.apache.camel.Exchange;

public class CamelCORSManager {
    private CamelCORSManager(){}

    public static void configure(Exchange exchange){
        exchange.getMessage().setHeader("Access-Control-Allow-Credentials", "true");
        exchange.getMessage().setHeader("Access-Control-Allow-Headers", "*");
        exchange.getMessage().setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        exchange.getMessage().setHeader("Access-Control-Allow-Origin", "*");
    }

}
