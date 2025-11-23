package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Component;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_JAVA_SERVICE_LIST;

@Component
public class JavaServiceManagementService extends AbstractJavaService {

private List<AbstractJavaService> javaServiceList;
    public JavaServiceManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper,List<AbstractJavaService> javaServiceList) {
        super(producerTemplate, objectMapper);
        this.javaServiceList = javaServiceList;
    }

    @ir.daneshrefah.scm.common.annotation.JavaService(operationCode = SVC_JAVA_SERVICE_LIST)
    public List<String> findAll() {
        return  javaServiceList
                .stream()
                .map(abstractJavaService -> abstractJavaService.getClass().getSimpleName())
                .toList();
    }
}
