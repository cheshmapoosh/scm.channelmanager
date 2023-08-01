package ir.daneshrefah.scm.core.integration.component;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.core.service.ErrorMappingService;
import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.io.ClassLoader;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class ServiceComponentPoolGenerator extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceComponentPoolGenerator.class);

    private ServiceComponentProvider serviceComponentProvider;
    private List<ServiceComponent> serviceComponents;
    private ErrorMappingService errorMappingService;

    public ServiceComponentPoolGenerator(ServiceComponentProvider serviceComponentProvider,
                                         List<ServiceComponent> serviceComponents,
                                         ErrorMappingService errorMappingService) {
        this.serviceComponentProvider = serviceComponentProvider;
        this.serviceComponents = serviceComponents;
        this.errorMappingService = errorMappingService;
    }

    @Override
    public void configure() {
        for (Iterator<ServiceComponent> iterator = serviceComponents.iterator(); iterator.hasNext(); ) {
            ServiceComponent serviceComponent = iterator.next();
            AbstractTransformer requestTransformer = TransformerType.JAVA.equals(serviceComponent.getRequestTransformerType()) ?
                    ClassLoader.createInstanceOfClass(serviceComponent.getRequestTransformerClass(), AbstractTransformer.class) :
                    null;
            AbstractTransformer responseTransformer = TransformerType.JAVA.equals(serviceComponent.getResponseTransformerType()) ?
                    ClassLoader.createInstanceOfClass(serviceComponent.getResponseTransformerClass(), AbstractTransformer.class) :
                    null;

            String fromUri = "SVC_" + serviceComponentProvider.getCode() + "_" + serviceComponent.getCode();
            String toUri = serviceComponentProvider.getCode() + ":" + serviceComponent.getCode();

            from("direct:" + fromUri)
                    .routeId("ROUTE_" + fromUri)
                    .onException(Exception.class)
                    .process(exchange -> {
                        Message message = exchange.getMessage().getBody(Message.class);
                        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        if (exception instanceof BaseException) {
                            BaseException baseException = (BaseException) exception;
                            message = errorMappingService.resolveMessageByErrorCode(message, baseException);
                        } else {
                            message = errorMappingService.resolveMessageByException(message, exception);
                        }
                        exchange.getMessage().setBody(message);
                    })
                    .end()
                    .choice()
                    .when(simple("${body.status} == 'SC_PROCESSING'"))
                        .process(exchange -> {
                            // request transformer process
                            if (null != requestTransformer) {
                                Message message = exchange.getMessage().getBody(Message.class);
                                Object request = requestTransformer.transform(
                                        serviceComponent.getRequestJSONSchema(), null, message,
                                        serviceComponent.getRequestMetadata());
                                message.getMessageComponent().setPayload(request);
                            }

                        })
                        .to(toUri)
                        .process(exchange -> {
                            // response transformer process
                            Message message = exchange.getMessage().getBody(Message.class);
                            if (null != responseTransformer) {
                                JsonNode response = (JsonNode) responseTransformer.transform(null,
                                        serviceComponent.getResponseJSONSchema(),
                                        message, serviceComponent.getResponseMetadata());
                                if (null != response)
                                    message.getMessageComponent().setPayload(response);
                            }

                        })
                    .otherwise()
                    .endChoice()
                    .end();
            LOGGER.info("serviceComponent with source '{}' and target '{}' registered.", fromUri, toUri);
        }

    }

}
