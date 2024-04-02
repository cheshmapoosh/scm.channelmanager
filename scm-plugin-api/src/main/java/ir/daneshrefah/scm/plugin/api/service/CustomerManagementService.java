package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.service.MembershipFindRequest;
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

    public List<MembershipTerminalAccess> findLocalMembershipTerminalAccesses(MembershipFindRequest request) {
        return customerService.findLocalMembershipTerminalAccesses(request);
    }
    /*public Customer findCustomerByProviderIdAndPersonId(String providerId, Long personId) {
        User user = AuthenticationUtils.getLoggedInUser(MessageContext.getCurrentContext().getMessage());
        return customerService.findLocalCustomerByProviderIdAndPersonId(providerId, personId);
    }

    public List<AccountAsset> findCustomerAccountList() {
        UserAuthentication userAuthentication = AuthenticationUtils.getLoggedInUserAuthentication(MessageContext.getCurrentContext().getMessage());
        return customerService.findLocalCustomerAssetListByPersonId(userAuthentication.getPersonProfile().getPersonId(), AccountAsset.class);
    }

    public Customer synchronizeCustomerInfoByProviderIdAndPersonId(CustomerSynchronizationRequest request) {
        return customerService.synchronizeProviderCustomerInfoByPersonId(request);
    }*/

}
