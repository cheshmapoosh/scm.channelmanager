package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.mq.jms.JakarataConnectionFactory;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@AllArgsConstructor
public class OtpDeviceConfig {

    private final OtpDeviceProperties otpDeviceProperties;

    @Bean(name = "avacasOtpConnectionFactory")
    public ConnectionFactory avacasMqConnectionFactory() throws JMSException {
        JakarataConnectionFactory factory = new JakarataConnectionFactory();
        factory.setQueueManager(otpDeviceProperties.getQueueManager());
        factory.setHostName(otpDeviceProperties.getHostName());
        factory.setPort(otpDeviceProperties.getPort());
        factory.setTransportType(1);
        factory.setChannel(otpDeviceProperties.getChannel());
        factory.setUsername(otpDeviceProperties.getUsername());
        factory.setPassword(StringUtils.isBlank(otpDeviceProperties.getPassword()) ? null : otpDeviceProperties.getPassword());
        return factory;
    }

    @Bean("avacasJmsTemplate")
    public JmsTemplate avacasJmsTemplate(@Qualifier("avacasOtpConnectionFactory") ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setTimeToLive(300000);
        jmsTemplate.setDeliveryMode(1);
        jmsTemplate.setSessionTransacted(false);
        jmsTemplate.setExplicitQosEnabled(true);
        return jmsTemplate;
    }
}
