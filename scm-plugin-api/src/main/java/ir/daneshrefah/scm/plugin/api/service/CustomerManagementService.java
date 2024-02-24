package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.plugin.api.inbound.MessageContext;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import org.springframework.stereotype.Service;

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

    public Customer findCustomerByProviderIdAndPersonId(String providerId, Long personId) {
        User user = AuthenticationUtils.getLoggedInUser(MessageContext.getCurrentContext().getMessage());
        return customerService.findCustomerByProviderIdAndPersonId(providerId, personId);
    }

    public Customer synchronizeCustomerInfoByProviderIdAndPersonId(CustomerSynchronizationRequest request) {
        return customerService.synchronizeProviderCustomerInfoByPersonId(request);
    }

}
