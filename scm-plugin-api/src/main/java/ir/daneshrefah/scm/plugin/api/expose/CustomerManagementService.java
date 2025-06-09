package ir.daneshrefah.scm.plugin.api.expose;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityRequest;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityResponse;
import ir.daneshrefah.scm.common.dto.asset.*;
import ir.daneshrefah.scm.common.dto.membership.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@Controller("/")
public class CustomerManagementService extends AbstractJavaService {

    private final CustomerService customerService;

    public CustomerManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, CustomerService customerService) {
        super(producerTemplate, objectMapper);
        this.customerService = customerService;
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'
    @JavaService(serviceCode = SVC_ASSETS_SYNC)
    @SuppressWarnings("unused")
    public List<Membership> syncMembershipList(CustomerSyncRequest request){
        return customerService.syncMembershipList(request);
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'
    @JavaService(serviceCode = SVC_ASSETS_ASSIGN_MEMBERSHIP_CHANNEL)
    @SuppressWarnings("unused")
    public List<String> assignMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request){
        return customerService.assignMembershipTerminalAccess(request);
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'
    @JavaService(serviceCode = SVC_ASSETS_REVOKE_MEMBERSHIP_CHANNEL)
    @SuppressWarnings("unused")
    public List<String> revokeMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request){
        return customerService.revokeMembershipTerminalAccess(request);
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'
    @JavaService(serviceCode = SVC_ASSETS_MEMBERSHIP_CHL_WDR_LIMIT)
    @SuppressWarnings("unused")
    public Membership updateMembershipTerminalAccessMaxWithdrawal(MembershipTerminalAccessWithdrawalLimitUpdateRequest request){
        return customerService.updateMembershipTerminalAccessMaxWithdrawal(request);
    }

    @JavaService(serviceCode = SVC_ASSETS_FAVOURITE)
    @SuppressWarnings("unused")
    public AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request){
        return customerService.accountFavoriteActivity(request);
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
    public Membership findAccountMembershipById(String membershipId){
        return customerService.findLocalAccountMembership(membershipId);
    }
    /* MEMBERSHIP CHANNEL ACCESS */

    @JavaService(serviceCode = SVC_ASSETS_MCA_LIST)
    @SuppressWarnings("unused")
    public PagedResponseData<MembershipTerminalAccessDto> findMembershipTerminalAccessList(MembershipLocalFindRequest request){
        List<MembershipTerminalAccessDto> result = customerService.findLocalMembershipTerminalAccesses(request);
        return new PagedResponseData<>(request,result);
    }

    @JavaService(serviceCode = SVC_ASSETS_MCA_EDIT)
    @SuppressWarnings("unused")
    public MembershipTerminalAccessDto editMembershipTerminalAccessList(MembershipLocalEditRequest request){
        return customerService.editMembershipTerminalAccesses(request);
    }

    @JavaService(serviceCode = SVC_ASSETS_MCA_GET)
    @SuppressWarnings("unused")
    public MembershipTerminalAccessDto get(@Valid @Numeric String id){
        return customerService.getMembershipChannelAccess(Long.parseLong(id));
    }


    /* CHANNEL SERVICE ACCESS */

    @JavaService(serviceCode = SVC_SERVICE_CHANNEL_ACCESS_LIST)
    public List<ChannelServiceAccess> findChannelServiceAccessByTerminal(ChannelServiceAccessFindRequest request){
        return customerService
                .findAllChannelServiceAccessList(request)
                .stream()
                .peek(model->model.setChannel(null))
                .toList();
    }

    @JavaService(serviceCode = SVC_SERVICE_CATEGORY_LIST)
    public List<ServiceCategory> findAllServiceCategory(){
        return customerService.findAllServiceCategory();
    }

    /* MEMBERSHIP CHANNEL SERVICE ACCESS */

    @JavaService(serviceCode = SVC_MCSA_LIST)
    public List<MembershipTerminalServiceAccessDto> findAllMembershipChannelServiceAccessList(MembershipChannelServiceAccessFindRequest request){
        return customerService.findAllMembershipChannelServiceAccessList(request);
    }

    @JavaService(serviceCode = SVC_MCSA_ASSIGNMENT)
    public List<MembershipTerminalServiceAccessDto> membershipChannelAccessServiceAssignment(MembershipTerminalServiceAssignmentRequest request){
        return customerService.membershipChannelAccessServiceAssignment(request);
    }


}
