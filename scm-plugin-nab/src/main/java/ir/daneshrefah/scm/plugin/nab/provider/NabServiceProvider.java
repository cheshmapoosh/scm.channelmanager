//package ir.daneshrefah.scm.plugin.nab.provider;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.daneshrefah.scm.common.model.message.Message;
//import ir.daneshrefah.scm.common.model.message.MessageOutput;
//import ir.daneshrefah.scm.common.model.service.HttpContentType;
//import ir.daneshrefah.scm.common.model.service.HttpMethod;
//import ir.daneshrefah.scm.common.service.ResourceService;
//import ir.daneshrefah.scm.common.service.ServiceService;
//import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.AbstractBaseRestExternalServiceProviderExecutor;
//import ir.daneshrefah.scm.plugin.nab.transformer.NabRequestTransformer;
//import ir.daneshrefah.scm.plugin.nab.transformer.NabResponseTransformer;
//import lombok.SneakyThrows;
//import org.apache.camel.CamelContext;
//import org.apache.camel.ExchangePropertyKey;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.impl.DefaultCamelContext;
//import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//
///**
// * Description of the class or purpose of the file.
// *
// * @author reza jamshidi
// * @version 1.0
// * @since 2023-08-05
// */
//@Component("nabCoreServiceProvider")
//public final class NabServiceProvider extends AbstractBaseRestExternalServiceProviderExecutor {
//
//    private final NabRequestTransformer requestTransformer;
//    private final NabResponseTransformer responseTransformer;
//
//    public NabServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService, NabRequestTransformer requestTransformer, NabResponseTransformer responseTransformer) {
//        super(objectMapper, resourceService, serviceService);
//        this.requestTransformer = requestTransformer;
//        this.responseTransformer = responseTransformer;
//    }
//
//
////    @Override
////    protected List<AbstractJsonTransformer> prepareRequestTransformers() {
////        return List.of(requestTransformer);
////    }
////
////    @Override
////    protected List<AbstractJsonTransformer> prepareResponseTransformers() {
////        return List.of(responseTransformer);
////    }
//
//    @Override
//    @SneakyThrows
//    protected String extractTargetUrl(Message message) {
//        return getProviderEndpoint().orElse(null);
//    }
//
//    @Override
//    protected Optional<String> extractQueryString(Message message) {
//        return Optional.empty();
//    }
//
//    @Override
//    protected Optional<Map<String, ?>> extractRequestHeaders(Message message) {
//        return Optional.empty();
//    }
//
//    @Override
//    protected Optional<Map<String, ?>> extractResponseHeaders(Message message) {
//        return Optional.empty();
//    }
//
//    @Override
//    protected HttpMethod extractHttpMethod(Message message) {
//        return HttpMethod.POST;
//    }
//
//    @Override
//    protected HttpContentType extractContentType(Message message) {
//        return null;
//    }
//
//    public static void main(String[] args) throws Exception {
//        CamelContext context = new DefaultCamelContext();
//
//        context.addRoutes(new RouteBuilder() {
//            @Override
//            public void configure() {
//                onException(Throwable.class)
//                        .process(exchange -> {
//                            exchange.getException();
//                        })
//                        .handled(true);
//                from("timer://firstSend?repeatCount=1")
//                        .to("direct:secondSend")
//                ;
//
//                // First send
////                from("timer://firstSend?repeatCount=1") // Sends only once
////                        .setBody(constant("ATPS"))
////                        .to("netty:tcp://10.15.27.12:3080?sync=true&reuseChannel=true")
////                        .process(exchange -> System.out.println("First: " + exchange.getMessage().getBody()))
////                        .log("First message sent")
////                        .to("direct:secondSend")
////                ;
////
////                // Second send
//                from("direct:secondSend") // Second send with a 5-second delay
//                        .process(exchange -> {
////                            exchange.getIn().setHeader("txnId", "1");
//                        })
//                        .idempotentConsumer(header("txnId"), new MemoryIdempotentRepository())
//                        .skipDuplicate(false)
//                        .choice()
//                        // When the message is not a duplicate, process it normally
//                            .when(exchange -> {
//                              Boolean duplicate = exchange.getProperty(ExchangePropertyKey.DUPLICATE_MESSAGE, Boolean.class);
//                              return Objects.equals(duplicate, Boolean.TRUE);
//                            })
//                                .log("Duplicate transaction detected: ${header.txnId}")
//                        // Otherwise, handle the duplicate
//                            .otherwise()
//                                .log("Processing new transaction: ${header.txnId}")
//                        .endChoice()
//                        .end();
//            }
//        });
//
//        context.start();
//        Thread.sleep(5000); // Keep the route alive for a while
//        context.stop();
//    }
//
//    @Override
//    protected Object extractServiceParametersResponseBody(Message message, Object body) {
//        return null;
//    }
//
//    @Override
//    protected Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput) {
//        return null;
//    }
//}
