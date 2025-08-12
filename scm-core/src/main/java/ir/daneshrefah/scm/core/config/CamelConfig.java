package ir.daneshrefah.scm.core.config;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-15
 */
@Component
public class CamelConfig implements CamelContextConfiguration {

    private final CamelContext camelContext;

    @Autowired
    public CamelConfig(CamelContext camelContext) {
        this.camelContext = camelContext;
        this.camelContext.setTracing(true);
    }

    @Override
    public void beforeApplicationStart(CamelContext camelContext) {
//        String threadNamePattern = "Camel (" + camelContext.getName() + ") thread ##counter# - #name#";
        String threadNamePattern = "scm_" + camelContext.getName() + "_thread##counter#_#name#";
        camelContext.getExecutorServiceManager().setThreadNamePattern(threadNamePattern);
        camelContext.getManagementStrategy().addEventNotifier(new CamelEventNotifierSupport());
        camelContext.addLifecycleStrategy(new CamelLifecycleStrategy());
//        camelContext.setMessageHistory(true);
//        camelContext.setSourceLocationEnabled(true);
    }

    @Override
    public void afterApplicationStart(CamelContext camelContext) {

    }

}
