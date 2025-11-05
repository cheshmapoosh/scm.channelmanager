package ir.daneshrefah.scm.plugin.camel.config;

import ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps.AtpsComponent;
import ir.daneshrefah.scm.plugin.camel.component.webclient.WebClientComponent;
import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(CamelContext.class)
public class ScmCamelComponentsAutoConfiguration {

    @Bean("webclient")
    public WebClientComponent webClientComponent(CamelContext camelContext) {
        WebClientComponent component = new WebClientComponent();
        component.setCamelContext(camelContext);
        return component;
    }
    @Bean("atps")
    public AtpsComponent atpsComponent(CamelContext camelContext) {
        AtpsComponent component = new AtpsComponent();
        component.setCamelContext(camelContext);
        return component;
    }
}
