package ir.daneshrefah.scm.gateway.service;

import ir.daneshrefah.scm.common.model.GatewayOperation;
import ir.daneshrefah.scm.gateway.repository.GatewayOperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GatewayOperationService {

    @Autowired
    private GatewayOperationRepository gatewayOperationRepository;
    public Map<String, GatewayOperation> findAllGatewayOperation() {
        return gatewayOperationRepository.findAllGatewayOperation();
    }

}
