package ir.daneshrefah.scm.core.inbound;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */

@Configuration
public class InboundChannelsAutoConfiguration {

    @Autowired
    private ConfigurableBeanFactory beanFactory;
    
//    @Bean
//    public List<AbstractInboundChannelGenerator> inboundChannelGenerator() {
//        List<AbstractInboundChannelGenerator> inboundChannelGenerators = new ArrayList<>();
//        inboundChannelGenerators.add(new RestInboundChannelGenerator());
//        return inboundChannelGenerators;
//    }

    @Bean
    public void restInboundChannelGenerator() {
        beanFactory.registerSingleton("testbean", new RestInboundChannelGenerator());

//        List<RestInboundChannelGenerator> inboundChannelGenerators = new ArrayList<>();
//        inboundChannelGenerators.add(new RestInboundChannelGenerator());
//        return inboundChannelGenerators;
    }
}
