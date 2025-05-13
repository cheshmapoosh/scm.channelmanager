package ir.daneshrefah.scm.plugin.api.integration;

import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
public abstract class ServiceProviderDataProvider {

    protected AbstractAuditableExternalServiceProvider provider;

    public void init(AbstractAuditableExternalServiceProvider provider) {
        this.provider = provider;
    }

    /*public abstract Customer inquireRemoteCustomerByPerson(GeneralPerson person);

    public abstract Customer inquireRemoteCustomerByPerson(PersonType personType, Nationality nationality, String nationalId,
                                                           String subOrganizationId);

    public abstract Customer inquireRemoteCustomerAssetList(PersonType personType, Nationality nationality, String nationalId,
                                                           String subOrganizationId);

    public abstract Customer findLocalCustomerByPersonId(Long personId);

    public abstract Customer findLocalCustomerByPersonProfileId(String personProfileId);

    public abstract <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz);

    public abstract Customer synchronizeCustomerInfo(ExternalServiceProvider provider, GeneralPerson person);*/

}
