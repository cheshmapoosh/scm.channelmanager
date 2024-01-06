package ir.daneshrefah.scm.logging.impl;

import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-04
 */
@Component
@ConditionalOnProperty(name = "scm.log.type", havingValue = "kafka")
public class KafkaEventProducer implements EventProducer {

    @Override
    public void sendEvent(Event event) {


    }

}
