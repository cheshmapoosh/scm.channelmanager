package ir.daneshrefah.scm.log.config;

import org.springframework.jms.annotation.EnableJms;

@EnableJms
//@ConditionalOnProperty(name = "scm.mq.ib", havingValue = "MQ", matchIfMissing = true)
public class MQConfig {
}