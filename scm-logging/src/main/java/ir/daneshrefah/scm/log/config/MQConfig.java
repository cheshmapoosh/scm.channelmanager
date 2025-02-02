package ir.daneshrefah.scm.log.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@ConditionalOnProperty(name = "scm.mq.enabled", havingValue = "true")
public class MQConfig {
}