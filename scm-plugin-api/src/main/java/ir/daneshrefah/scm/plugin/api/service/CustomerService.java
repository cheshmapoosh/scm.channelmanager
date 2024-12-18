package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.dto.membership.CustomerProviderSyncRequest;
import ir.daneshrefah.scm.common.model.asset.AccountMembership;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityRequest;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityResponse;
import ir.daneshrefah.scm.common.dto.membership.MembershipFindRequest;
import ir.daneshrefah.scm.common.dto.membership.MembershipLocalFindRequest;

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
    AccountMembership findLocalAccountMembership(String membershipId);

    List<MembershipTerminalAccess> findLocalMembershipTerminalAccesses(MembershipLocalFindRequest request);

    List<Membership> syncMembershipList(CustomerProviderSyncRequest request);
    List<Membership> findLocalMembershipList(MembershipLocalFindRequest request);
    List<Membership> findMembershipList(MembershipFindRequest request);
    AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request);


//    Customer findLocalCustomerByProviderIdAndPersonId(String providerId, Long personId);
//    Customer findLocalCustomerByProviderIdAndPersonUsername(String providerId, String username);
//    Customer findLocalCustomerByProviderIdAndPersonId(String providerId, PersonProfile.PersonId personId);
//    <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz);
//    Customer findRemoteCustomerByProviderIdAndPersonId(String providerId, Long personId);
//    Customer synchronizeProviderCustomerInfoByPersonId(CustomerSynchronizationRequest request);
//
}
