package ir.daneshrefah.scm.plugin.api.integration;

import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
public abstract class ServiceProviderDataProvider {

    protected ExternalServiceProvider provider;

    public void init(ExternalServiceProvider provider) {
        this.provider = provider;
    }

    public abstract Customer inquireCustomerByPerson(GeneralPerson person);

    public abstract Customer inquireCustomerByPerson(PersonType personType, Nationality nationality, String nationalId,
                                                     String subOrganizationId);

    public abstract Customer findCustomerByPersonId(Long personId);

    public abstract Customer findCustomerByPersonProfileId(String personProfileId);

}
