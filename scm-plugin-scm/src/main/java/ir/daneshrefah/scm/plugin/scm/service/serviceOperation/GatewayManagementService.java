package ir.daneshrefah.scm.plugin.scm.service.serviceOperation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GatewayManagementService extends AbstractJavaService {

    private final GatewayService gatewayService;

    public GatewayManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, GatewayService gatewayService) {
        super(producerTemplate, objectMapper);
        this.gatewayService = gatewayService;
    }

    @JavaService(operationCode = OperationCode.SVC_GATEWAY_CHANNEL_LIST)
    public List<GatewayChannel> findAll() {
        return gatewayService.findAll();
    }
}
