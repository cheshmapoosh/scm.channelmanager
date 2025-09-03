package ir.daneshrefah.scm.plugin.api.expose;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.*;
import ir.daneshrefah.scm.common.dto.asset.*;
import ir.daneshrefah.scm.common.dto.membership.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

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

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'

    // POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/sync'
    @JavaService(operationCode = SVC_ASSETS_SYNC)
    @SuppressWarnings("unused")
    public List<Membership> syncMembershipList(CustomerSyncRequest request){
        return customerService.syncMembershipList(request);
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'

    // POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/assign-channel-access'
    @JavaService(operationCode = SVC_ASSETS_ASSIGN_MEMBERSHIP_CHANNEL)
    @SuppressWarnings("unused")
    public List<String> assignMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request){
        return customerService.assignMembershipTerminalAccess(request);
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'

    // POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/revoke-channel-access'
    @JavaService(operationCode = SVC_ASSETS_REVOKE_MEMBERSHIP_CHANNEL)
    @SuppressWarnings("unused")
    public List<String> revokeMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request){
        return customerService.revokeMembershipTerminalAccess(request);
    }

    //TODO RULE ACCESS CHECK 'ROLE_ADMIN_CUSTOMER'

//    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/update-channel-access/max-withdrawal'
    @JavaService(operationCode = SVC_ASSETS_MEMBERSHIP_CHL_WDR_LIMIT)
    @SuppressWarnings("unused")
    public Membership updateMembershipTerminalAccessMaxWithdrawal(MembershipTerminalAccessWithdrawalLimitUpdateRequest request){
        return customerService.updateMembershipTerminalAccessMaxWithdrawal(request);
    }

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/account/favorite-modification'
    @JavaService(operationCode = SVC_ASSETS_FAVOURITE)
    @SuppressWarnings("unused")
    public AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request){
        return customerService.accountFavoriteActivity(request);
    }

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/default-account'
    @JavaService(operationCode = SVC_ASSETS_DEFAULT_ACCOUNT)
    @SuppressWarnings("unused")
    public ChangeDefaultAccountStatusResponse setDefaultAccount(ChangeDefaultAccountStatusRequest request){
        return customerService.setDefaultAccount(request);
    }

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/default-account/remove'
    @JavaService(operationCode = SVC_ASSETS_REMOVE_DEFAULT_ACCOUNT)
    @SuppressWarnings("unused")
    public ChangeDefaultAccountStatusResponse removeDefaultAccount(ChangeDefaultAccountStatusRequest request){
        return customerService.removeDefaultAccount(request);
    }

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/default-account/list'
    @JavaService(operationCode = SVC_ASSETS_SHOW_DEFAULT_ACCOUNT)
    @SuppressWarnings("unused")
    public PagedResponseData<DefaultAccountStatusListResponse> defaultAccountList(DefaultAccountStatusListRequest request){
        return new PagedResponseData<>(request,customerService.defaultAccountList(request));
    }


    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/local-list'
    @JavaService(operationCode = SVC_ASSETS_LIST_LOCAL)
    @SuppressWarnings("unused")
    public PagedResponseData<Membership> findLocalMembershipList(MembershipLocalFindRequest request){
        List<Membership> result = customerService.findLocalMembershipList(request);
        return new PagedResponseData<>(request,result);
    }


    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/list'
    @JavaService(operationCode = SVC_ASSETS_LIST)
    @SuppressWarnings("unused")
    public PagedResponseData<Membership> findMembershipList(MembershipFindRequest request){
        List<Membership> result = customerService.findMembershipList(request);
        return new PagedResponseData<>(request,result);
    }

    //    GET 'http://127.0.0.1:8083/scm4test/api/v1/assets/local-account/{membershipId}'
    @JavaService(operationCode = SVC_ASSETS_FIND_ACCOUNT_MEMBERSHIP)
    @SuppressWarnings("unused")
    public Membership findAccountMembershipById(String membershipId){
        return customerService.findLocalAccountMembership(membershipId);
    }
    /* MEMBERSHIP CHANNEL ACCESS */

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/mca/list'
    @JavaService(operationCode = SVC_ASSETS_MCA_LIST)
    @SuppressWarnings("unused")
    public PagedResponseData<MembershipTerminalAccessDto> findMembershipTerminalAccessList(MembershipLocalFindRequest request){
        List<MembershipTerminalAccessDto> result = customerService.findLocalMembershipTerminalAccesses(request);
        return new PagedResponseData<>(request,result);
    }

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/mca/edit'
    @JavaService(operationCode = SVC_ASSETS_MCA_EDIT)
    @SuppressWarnings("unused")
    public MembershipTerminalAccessDto editMembershipTerminalAccessList(MembershipLocalEditRequest request){
        return customerService.editMembershipTerminalAccesses(request);
    }

    //    GET 'http://127.0.0.1:8083/scm4test/api/v1/assets/mca/get/{id}'
    @JavaService(operationCode = SVC_ASSETS_MCA_GET)
    @SuppressWarnings("unused")
    public MembershipTerminalAccessDto get(@Valid @Numeric String id){
        return customerService.getMembershipChannelAccess(Long.parseLong(id));
    }


    /* CHANNEL SERVICE ACCESS */

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/channel-service-access/list'
    @JavaService(operationCode = SVC_SERVICE_CHANNEL_ACCESS_LIST)
    public List<ChannelServiceAccess> findChannelServiceAccessByTerminal(ChannelServiceAccessFindRequest request){
        return customerService
                .findAllChannelServiceAccessList(request)
                .stream()
                .peek(model->model.setChannel(null))
                .toList();
    }

    //    GET 'http://127.0.0.1:8083/scm4test/api/v1/assets/service-category/list'
//    @JavaService(operationCode = SVC_SERVICE_CATEGORY_LIST)
//    public List<ServiceCategory> findAllServiceCategory(){
//        return customerService.findAllServiceCategory();
//    }

    /* MEMBERSHIP CHANNEL SERVICE ACCESS */

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/mcsa/find'
    @JavaService(operationCode = SVC_MCSA_LIST)
    public List<MembershipTerminalServiceAccessDto> findAllMembershipChannelServiceAccessList(MembershipChannelServiceAccessFindRequest request){
        return customerService.findAllMembershipChannelServiceAccessList(request);
    }

    //    POST 'http://127.0.0.1:8083/scm4test/api/v1/assets/mcsa/service-assignment'
    @JavaService(operationCode = SVC_MCSA_ASSIGNMENT)
    public List<MembershipTerminalServiceAccessDto> membershipChannelAccessServiceAssignment(MembershipTerminalServiceAssignmentRequest request){
        return customerService.membershipChannelAccessServiceAssignment(request);
    }


}
