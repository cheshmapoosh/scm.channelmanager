package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.service.MembershipFindRequest;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-21
 */
public interface CustomerService {

    List<MembershipTerminalAccess> findMembershipTerminalAccessList(Long personId, String terminalId);

    List<MembershipTerminalAccess> findLocalMembershipTerminalAccesses(MembershipFindRequest request);

//    Customer findLocalCustomerByProviderIdAndPersonId(String providerId, Long personId);
//    Customer findLocalCustomerByProviderIdAndPersonUsername(String providerId, String username);
//    Customer findLocalCustomerByProviderIdAndPersonId(String providerId, PersonProfile.PersonId personId);
//    <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz);
//    Customer findRemoteCustomerByProviderIdAndPersonId(String providerId, Long personId);
//    Customer synchronizeProviderCustomerInfoByPersonId(CustomerSynchronizationRequest request);
//
}
