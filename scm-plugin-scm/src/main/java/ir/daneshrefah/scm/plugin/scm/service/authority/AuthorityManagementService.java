package ir.daneshrefah.scm.plugin.scm.service.authority;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.service.authority.AuthorityService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorityManagementService extends AbstractJavaService {

    private final AuthorityService authorityService;

    public AuthorityManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, AuthorityService authorityService) {
        super(producerTemplate, objectMapper);
        this.authorityService = authorityService;
    }

    @JavaService(operationCode = OperationCode.SCV_AUTHORITY_CONFIG_LIST)
    public List<String> getAuthorizationConfigList() {
        return authorityService.getAuthorizationConfigList();
    }

    @JavaService(operationCode = OperationCode.SCV_AUTHORITY_LIST)
    public List<String> getAuthorityList() {
        return authorityService.getAuthorityList();
    }
}
