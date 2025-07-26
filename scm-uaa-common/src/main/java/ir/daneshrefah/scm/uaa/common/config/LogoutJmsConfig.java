package ir.daneshrefah.scm.uaa.common.config;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQTopic;
import com.ibm.msg.client.wmq.WMQConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scm.jms.logout.enabled", havingValue = "true")
public class LogoutJmsConfig {

    private final LogoutJmsConfigProperties properties;

    @Bean(name = "logoutConnectionFactory")
    public ConnectionFactory logoutMqConnectionFactory() throws JMSException {
        MQConnectionFactory factory = new MQConnectionFactory();
        factory.setQueueManager(properties.getQueueManager());
        factory.setHostName(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        factory.setChannel(properties.getChannel());
        if (StringUtils.isNotBlank(properties.getUsername())) {
            factory.setStringProperty(WMQConstants.USERID, properties.getUsername());
        }
        if (StringUtils.isNotBlank(properties.getPassword())) {
            factory.setStringProperty(WMQConstants.PASSWORD, properties.getPassword());
        }
        return factory;
    }

    @Bean(name = "logoutJmsTemplate")
    public JmsTemplate logoutJmsTemplate(@Qualifier("logoutConnectionFactory") ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setDefaultDestinationName(properties.getTopic());
        jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        jmsTemplate.setSessionTransacted(false);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }

    @Bean(name = "logoutTopic")
    public Destination logoutTopic() throws JMSException {
        return new MQTopic(properties.getTopic());
    }
}
