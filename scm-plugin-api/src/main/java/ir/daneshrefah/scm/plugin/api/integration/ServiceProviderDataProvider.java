package ir.daneshrefah.scm.plugin.api.integration;

import ir.daneshrefah.scm.common.model.customer.Asset;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;

import java.util.List;

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

    public abstract Customer inquireRemoteCustomerByPerson(GeneralPerson person);

    public abstract Customer inquireRemoteCustomerByPerson(PersonType personType, Nationality nationality, String nationalId,
                                                           String subOrganizationId);

    public abstract Customer inquireRemoteCustomerAssetList(PersonType personType, Nationality nationality, String nationalId,
                                                           String subOrganizationId);

    public abstract Customer findLocalCustomerByPersonId(Long personId);

    public abstract Customer findLocalCustomerByPersonProfileId(String personProfileId);

    public abstract <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz);

    public abstract Customer synchronizeCustomerInfo(ExternalServiceProvider provider, GeneralPerson person);

}
