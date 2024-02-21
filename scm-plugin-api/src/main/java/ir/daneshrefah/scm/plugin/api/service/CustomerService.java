package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-21
 */
public interface CustomerService {

//    PersonProfile fillCustomerForPersonProfile(PersonProfile profile, ExternalServiceProvider provider);

    Customer findCustomerByPersonId(ExternalServiceProvider provider, PersonProfile.PersonId personId);

    public Customer findCustomerByProviderCode(String providerCode, Long personId);
    public Customer findCustomerByProviderIdAndPersonId(String providerId, Long personId);

    Customer findCustomerByPersonProfileId(ExternalServiceProvider provider, String personProfileId);
    Customer findCustomerByPersonId(ExternalServiceProvider provider, Long personId);

    public Customer synchronizeProviderCustomerInfoByPersonId(ExternalServiceProvider provider, Integer personId);

}
