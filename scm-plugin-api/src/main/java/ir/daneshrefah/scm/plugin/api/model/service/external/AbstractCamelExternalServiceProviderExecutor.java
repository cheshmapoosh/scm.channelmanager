//package ir.daneshrefah.scm.plugin.api.model.service.external;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.daneshrefah.scm.common.model.message.Message;
//import ir.daneshrefah.scm.common.model.message.MessageOutput;
//import ir.daneshrefah.scm.common.service.ResourceService;
//import ir.daneshrefah.scm.common.service.ServiceService;
//import org.apache.camel.Exchange;
//import org.apache.camel.model.TryDefinition;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.Map;
//
//import static org.apache.camel.builder.Builder.simple;
//
///**
// * Description of the class or purpose of the file.
// *
// * @author reza jamshidi
// * @version 1.0
// * @since 2023-08-06
// */
//public abstract class AbstractCamelExternalServiceProviderExecutor extends AbstractExternalServiceProviderExecutor {
//
//    private static final String HEADER_TARGET_URL = "ScmTargetUrl";
//
//    public AbstractCamelExternalServiceProviderExecutor(ResourceService resourceService, ServiceService serviceService, ObjectMapper objectMapper) {
//        super(resourceService, serviceService, objectMapper);
//    }
//
//    @Override
//    public final void intiEndpointCallRouteDefinitionInternal(TryDefinition routeDefinition) {
//        routeDefinition.process(exchange -> {
//            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
//            MessageOutput messageOutput = exchange.getProperty(HEADER_MESSAGE_OUTPUT, MessageOutput.class);
//            messageOutput.setProviderUrl(extractTargetEndpointUrl(originalMessage));
//            messageOutput.setHeaders(extractRequestHeaders(originalMessage));
//            String providerUrl = messageOutput.getProviderUrl();
//            if (providerUrl.contains("?")){
//                //check query String
//                String[] split = StringUtils.split(providerUrl, "?");
//                providerUrl = split[0];
//                String queryString = split[1];
//                exchange.getMessage().setHeader(Exchange.HTTP_QUERY,  queryString);
//            }
//            exchange.getMessage().setHeader(HEADER_TARGET_URL,providerUrl );
//            Map<String, Object> headers = messageOutput.getHeaders();
//            if (null != headers && !headers.isEmpty()) {
//                for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext(); ) {
//                    String header = iterator.next();
//                    exchange.getMessage().setHeader(header, headers.get(header));
//                }
//            }
//
//        });
//        routeDefinition.toD("${header." + HEADER_TARGET_URL + "}");
//    }
//
//    protected Map<String, Object> extractRequestHeaders(Message message) {
//        return Collections.emptyMap();
//    }
//
//    protected abstract String extractTargetEndpointUrl(Message message);
//}
