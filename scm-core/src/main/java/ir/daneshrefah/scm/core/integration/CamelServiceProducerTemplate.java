package ir.daneshrefah.scm.core.integration;

import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Service
public class CamelServiceProducerTemplate implements ServiceProducerTemplate {

    @Autowired
    private ProducerTemplate producerTemplate;
    @Override
    public void callService(ir.daneshrefah.scm.common.model.service.Service service, Message message) {
        String serviceUrl = "direct:SVI_" + service.getCode();
        Exchange exchangeResult = producerTemplate.send(serviceUrl, exchange -> {
            exchange.getIn().setBody(message);
        });
    }
}
