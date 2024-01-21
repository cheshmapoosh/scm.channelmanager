package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-21
 */
public interface CustomerDataProviderService {

    public PersonProfile fillCustomerForPersonProfile(PersonProfile profile, ExternalServiceProvider provider);

}
