package ir.daneshrefah.scm.log.config;

import ir.daneshrefah.scm.mq.jms.JakarataConnectionFactory;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LogJmsConfig {

    private final LogJmsConfigProperties properties;

    @Bean(name = "jmsMqConnectionFactory")
    public ConnectionFactory logJmsConnectionFactory() throws JMSException {
        JakarataConnectionFactory factory = new JakarataConnectionFactory();
        factory.setQueueManager(properties.getQueueManager());
        factory.setHostName(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setTransportType(1);
        factory.setChannel(properties.getChannel());
        if (StringUtils.isNotBlank(properties.getUsername())) {
            factory.setUsername(properties.getUsername());
        }
        if (StringUtils.isNotBlank(properties.getPassword())) {
            factory.setPassword(properties.getPassword());
        }
        return factory;
    }

    @Bean(name = "logJmsTemplate")
    public JmsTemplate logJmsTemplate(@Qualifier("jmsMqConnectionFactory") ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestinationName(properties.getDestination());
        jmsTemplate.setTimeToLive(properties.getTimeToLive());
        jmsTemplate.setDeliveryMode(1);
        jmsTemplate.setSessionTransacted(false);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }
}
