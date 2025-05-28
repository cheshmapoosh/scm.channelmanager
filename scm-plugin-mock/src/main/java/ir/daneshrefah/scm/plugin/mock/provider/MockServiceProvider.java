//package ir.daneshrefah.scm.plugin.mock.provider;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.daneshrefah.scm.common.model.message.Message;
//import ir.daneshrefah.scm.common.model.message.MessageOutput;
//import ir.daneshrefah.scm.common.model.service.ScmService;
//import ir.daneshrefah.scm.common.service.ResourceService;
//import ir.daneshrefah.scm.common.service.ServiceService;
//import org.springframework.stereotype.Component;
//
///**
// * Description of the class or purpose of the file.
// *
// * @author reza jamshidi
// * @version 1.0
// * @since 2023-08-05
// */
//@Component("mockCoreServiceProvider")
//public class MockServiceProvider extends AbstractPureExternalServiceProviderExecutor {
//
//
//    public MockServiceProvider(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
//        super(objectMapper, resourceService, serviceService);
//    }
//
//    @Override
//    public JsonNode executeEndpoint(Message originalMessage, Object body) {
//        ScmService service = originalMessage.getHeader().getService();
////        serviceAccess.get
//        return null;
//    }
//
//    @Override
//    protected MessageOutput buildMessageOutput() {
//        return null;
//    }
//}
