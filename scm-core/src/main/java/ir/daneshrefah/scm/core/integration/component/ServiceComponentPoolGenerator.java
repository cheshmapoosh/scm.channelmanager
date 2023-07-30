package ir.daneshrefah.scm.core.integration.component;

import ir.daneshrefah.scm.core.service.ErrorMappingService;
import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
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
            String fromUri = "SVC_" + serviceComponentProvider.getCode() + "_" + serviceComponent.getCode();
            String toUri = serviceComponentProvider.getCode() + ":" + serviceComponent.getCode();
            from("direct:" + fromUri)
                    .routeId("ROUTE_" + fromUri)
                    .onException(Exception.class)
                    .process(exchange -> {
                        LOGGER.error("");
                        Message message = exchange.getMessage().getBody(Message.class);
                        String correlationId = message.getHeader().getCorrelationId();
                        String source = message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode();
                        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        if (exception instanceof BaseException) {
                            BaseException baseException = (BaseException) exception;
                            message = errorMappingService.resolveMessageByErrorCode(message, baseException);
                        } else {
                            message = errorMappingService.resolveMessageByException(message, exception);
                        }
//                        Error error = new Error("", "eror on ...", "");
//                        message.setPayload(null);
//                        message.addError(error, Status.SC_ERROR_UNAVAILABLE_PROVIDER.getCode());
//                        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, null);
                        exchange.getMessage().setBody(message);
//                        String code = StringUtils.isEmpty(exception.getMessage()) ? "" : exception.getMessage();
//                        Message m = new Message();
//                        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, null);
////                        exchange.getMessage().setBody("errrrrror");
////
//                        System.out.println("nowc");
                    })
                    .end()
                    .log("serviceComponent Call")
                    .to(toUri)
                    .end();
            LOGGER.info("serviceComponent with source '{}' and target '{}' registered.", fromUri, toUri);
        }

    }

}
