package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityRequest;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityResponse;
import ir.daneshrefah.scm.common.dto.membership.*;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-21
 */
public interface CustomerService {


    List<Membership> syncMembershipList(CustomerSyncRequest request);

    List<String> assignMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request);

    List<String> revokeMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request);

    Membership updateMembershipTerminalAccessMaxWithdrawal(MembershipTerminalAccessWithdrawalLimitUpdateRequest request);

    AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request);


    List<MembershipTerminalAccess> findMembershipTerminalAccessList(Long personId, String terminalId);

    Membership findLocalAccountMembership(String membershipId);

    List<MembershipTerminalAccess> findLocalMembershipTerminalAccesses(MembershipLocalFindRequest request);

    List<Membership> findLocalMembershipList(MembershipLocalFindRequest request);

    List<Membership> findMembershipList(MembershipFindRequest request);


//    Customer findLocalCustomerByProviderIdAndPersonId(String providerId, Long personId);
//    Customer findLocalCustomerByProviderIdAndPersonUsername(String providerId, String username);
//    Customer findLocalCustomerByProviderIdAndPersonId(String providerId, PersonProfile.PersonId personId);
//    <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz);
//    Customer findRemoteCustomerByProviderIdAndPersonId(String providerId, Long personId);
//    Customer synchronizeProviderCustomerInfoByPersonId(CustomerSynchronizationRequest request);
//
}
