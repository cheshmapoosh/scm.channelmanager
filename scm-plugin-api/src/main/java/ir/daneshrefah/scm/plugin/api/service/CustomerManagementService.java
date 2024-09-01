package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.service.person.CustomerProviderFindRequest;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.asset.AccountMembership;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.service.MembershipFindRequest;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@Service
public class CustomerManagementService extends AbstractJavaService {

    private final CustomerService customerService;

    public CustomerManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, CustomerService customerService) {
        super(producerTemplate, objectMapper);
        this.customerService = customerService;
    }

    @JavaService
    @SuppressWarnings("unused")
    public List<Membership> findProviderMembershipList(CustomerProviderFindRequest request){
        return customerService.findProviderMembershipList(request);
    }

    @JavaService
    @SuppressWarnings("unused")
    public PagedResponseData<Membership> findLocalMembershipList(MembershipFindRequest request){
        List<Membership> result = customerService.findLocalMembershipList(request);
        return new PagedResponseData<>(request,result);
    }

    @JavaService
    @SuppressWarnings("unused")
    public AccountMembership findAccountMembershipById(String membershipId){
        return customerService.findLocalAccountMembership(membershipId);
    }

}
