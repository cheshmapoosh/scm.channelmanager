package ir.daneshrefah.scm.gateway.camel;

import ir.daneshrefah.scm.nabconnectorimpl.component.NabComponent;
import jakarta.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelComponentConfig {

    @Autowired
    private CamelContext camelContext;

    @PostConstruct
    public void init() {
        camelContext.addComponent("nab", new NabComponent());
    }
}
