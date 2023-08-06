package ir.daneshrefah.scm.core.integration.component;

import ir.daneshrefah.scm.core.service.ServiceComponentService;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.MessageComponent;
import ir.daneshrefah.scm.plugin.api.service.ServiceProducerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
@Service
public class CamelServiceProducerTemplate implements ServiceProducerTemplate {

    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private ServiceComponentService serviceComponentService;

    @Override
    public void executeServiceComponent(String serviceComponentProviderCode, String serviceComponentCode,
                                        Message message, Object payload) {
        MessageComponent component = new MessageComponent();
        ServiceComponent serviceComponent = serviceComponentService.findServiceComponentByCodeAndProviderCode(
                    serviceComponentProviderCode, serviceComponentCode);
        component.setServiceComponent(serviceComponent);
        component.setPayload(payload);
        message.setMessageComponent(component);

        String serviceComponentUrl = "direct:SVC_" + serviceComponentProviderCode + "_" + serviceComponentCode;
        Exchange exchangeResult = producerTemplate.send(serviceComponentUrl, exchange -> {
            exchange.getIn().setBody(message);

//            exchange.getIn().setHeader("HeaderName", "HeaderValue");
        });
        System.out.println(exchangeResult);
    }

    @Override
    public void executeService(String serviceCode, Message message) {
        String serviceUrl = "direct:SVI_" + serviceCode;
        Exchange exchangeResult = producerTemplate.send(serviceUrl, exchange -> {
            exchange.getIn().setBody(message);

//            exchange.getIn().setHeader("HeaderName", "HeaderValue");
        });
    }

}
