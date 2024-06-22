package ir.daneshrefah.scm.mq.config;

import ir.daneshrefah.scm.mq.config.prop.IbmJmsConfigProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import ir.daneshrefah.scm.mq.jms.JakarataConnectionFactory;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageQueueConfig {
    private static final int IBM_MQ_DEFAULT_PORT = 1414;

    @Bean(name = "ibmConnectionFactory")
    @ConditionalOnProperty(value = "scm.ibm.mq.enabled",havingValue = "true")
    public ConnectionFactory ibmConnectionFactory(IbmJmsConfigProperty properties) throws JMSException {
        JakarataConnectionFactory factory = new JakarataConnectionFactory();
        factory.setQueueManager(properties.getQueueManager());
        factory.setHostName(properties.getHost());
        factory.setPort(properties.getPort()!=0 ? properties.getPort(): IBM_MQ_DEFAULT_PORT);
        factory.setTransportType(1);
        factory.setChannel(properties.getChannel());
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        return factory;
    }

}
