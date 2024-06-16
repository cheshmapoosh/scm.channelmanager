package ir.daneshrefah.scm.mq.config;

import ir.daneshrefah.scm.mq.config.prop.ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import ir.daneshrefah.scm.mq.jms.JakarataConnectionFactory;

import java.util.Objects;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageQueueConfig {
    private static final int IBM_MQ_DEFAULT_PORT = 1414;

    @Bean(name = "ibmConnectionFactory")
    @ConditionalOnProperty(name = "scm.mq.ibm-mq.host")
    public ConnectionFactory smsIbmMqConnectionFactory(ConfigProperties properties) throws JMSException {
        JakarataConnectionFactory factory = new JakarataConnectionFactory();
        factory.setQueueManager(properties.getIbmMq().getQueueManager());
        factory.setHostName(properties.getIbmMq().getHost());
        factory.setPort(properties.getIbmMq().getPort()!=0 ? properties.getIbmMq().getPort(): IBM_MQ_DEFAULT_PORT);
        factory.setTransportType(1);
        factory.setChannel(properties.getIbmMq().getChannel());
        factory.setUsername(properties.getIbmMq().getUsername());
        factory.setPassword(properties.getIbmMq().getPassword());
        return factory;
    }

    @Bean(name = "ibmConnectionFactory")
    @ConditionalOnProperty(name = "scm.mq.active-mq.host")
    public ConnectionFactory activeMqConnectionFactory() {
        return null;
    }

    @Bean(name = "ibmJmsTemplate")
    @ConditionalOnProperty(name = "scm.mq.enabled", havingValue = "true")
    public JmsTemplate smsJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestinationName("CM2SMS_R");
        jmsTemplate.setTimeToLive(300000);
        jmsTemplate.setDeliveryMode(1);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }
}
