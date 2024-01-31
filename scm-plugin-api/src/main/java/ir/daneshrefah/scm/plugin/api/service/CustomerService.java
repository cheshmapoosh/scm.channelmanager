package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
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

    Customer findCustomerByPersonProfileId(ExternalServiceProvider provider, String personProfileId);
    Customer findCustomerByPersonId(ExternalServiceProvider provider, Long personId);

    public Customer synchronizeProviderCustomerInfoByPersonId(ExternalServiceProvider provider, Long personId);

}
