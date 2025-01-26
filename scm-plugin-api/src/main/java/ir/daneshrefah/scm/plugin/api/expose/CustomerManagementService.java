package ir.daneshrefah.scm.plugin.api.expose;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.membership.CustomerProviderSyncRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.asset.AccountMembership;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityRequest;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityResponse;
import ir.daneshrefah.scm.common.dto.membership.MembershipFindRequest;
import ir.daneshrefah.scm.common.dto.membership.MembershipLocalFindRequest;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;

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

    @JavaService(serviceCode = SVC_ASSETS_SYNC)
    @SuppressWarnings("unused")
    public List<Membership> syncMembershipList(CustomerProviderSyncRequest request){
        return customerService.syncMembershipList(request);
    }

    @JavaService(serviceCode = SVC_ASSETS_LIST_LOCAL)
    @SuppressWarnings("unused")
    public PagedResponseData<Membership> findLocalMembershipList(MembershipLocalFindRequest request){
        List<Membership> result = customerService.findLocalMembershipList(request);
        return new PagedResponseData<>(request,result);
    }

    @JavaService(serviceCode = SVC_ASSETS_LIST)
    @SuppressWarnings("unused")
    public PagedResponseData<Membership> findMembershipList(MembershipFindRequest request){
        List<Membership> result = customerService.findMembershipList(request);
        return new PagedResponseData<>(request,result);
    }

    @JavaService(serviceCode = SVC_ASSETS_FIND_ACCOUNT_MEMBERSHIP)
    @SuppressWarnings("unused")
    public AccountMembership findAccountMembershipById(String membershipId){
        return customerService.findLocalAccountMembership(membershipId);
    }

    @JavaService(serviceCode = SVC_ASSETS_FAVOURITE)
    @SuppressWarnings("unused")
    public AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request){
        return customerService.accountFavoriteActivity(request);
    }

}
