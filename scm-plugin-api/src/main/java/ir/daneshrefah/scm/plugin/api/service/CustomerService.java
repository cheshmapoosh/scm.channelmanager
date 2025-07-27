package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.dto.*;
import ir.daneshrefah.scm.common.dto.asset.*;
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

    ChangeDefaultAccountStatusResponse setDefaultAccount(ChangeDefaultAccountStatusRequest request);

    ChangeDefaultAccountStatusResponse removeDefaultAccount(ChangeDefaultAccountStatusRequest request);

    List<DefaultAccountStatusListResponse> defaultAccountList(DefaultAccountStatusListRequest request);


    List<MembershipTerminalAccess> findMembershipChannelAccessList(Integer personId, Short channelId);

    Membership findLocalAccountMembership(String membershipId);

    List<MembershipTerminalAccessDto> findLocalMembershipTerminalAccesses(MembershipLocalFindRequest request);

    List<Membership> findLocalMembershipList(MembershipLocalFindRequest request);

    List<Membership> findMembershipList(MembershipFindRequest request);

    List<ServiceCategory> findAllServiceCategory();

    List<ChannelServiceAccess> findAllChannelServiceAccessList(ChannelServiceAccessFindRequest request);

    /* MCA */

    List<MembershipTerminalServiceAccessDto> findAllMembershipChannelServiceAccessList(MembershipChannelServiceAccessFindRequest request);

    MembershipTerminalAccessDto getMembershipChannelAccess(long l);

    MembershipTerminalAccessDto editMembershipTerminalAccesses(MembershipLocalEditRequest request);

    List<MembershipTerminalServiceAccessDto> membershipChannelAccessServiceAssignment(MembershipTerminalServiceAssignmentRequest request);


}
