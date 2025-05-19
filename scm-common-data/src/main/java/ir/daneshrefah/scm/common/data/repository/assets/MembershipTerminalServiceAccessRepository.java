package ir.daneshrefah.scm.common.data.repository.assets;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalServiceAccessEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipTerminalServiceAccessRepository extends JpaRepository<MembershipTerminalServiceAccessEntity, Integer> {
    @Query("""
            select o
            from MembershipTerminalServiceAccessEntity o
            where o.membershipTerminalAccess.channel.id = :channelId
              and o.membershipTerminalAccess.channel.parentId is null
              and o.channelServiceAccess.channel.parentId is null
              and o.channelServiceAccess.channel.id = :channelId
              and o.membershipTerminalAccess.membership.customerAccount.account.accountNo = :accountNo
              and o.membershipTerminalAccess.membership.person.id = :personId
            """)
    @EntityGraph(attributePaths = {
            "membershipTerminalAccess",
            "membershipTerminalAccess.channel",
            "membershipTerminalAccess.membership",
            "membershipTerminalAccess.membership.customerAccount",
            "membershipTerminalAccess.membership.customerAccount.account",
            "membershipTerminalAccess.membership.person",
            "channelServiceAccess",
            "channelServiceAccess.ebService"
    })
    List<MembershipTerminalServiceAccessEntity> findByLegacyTerminalIdAndAccountNo(@Param("channelId") Short channelId, @Param("accountNo") String accountNo,@Param("personId") Integer personId);


    @Query("select o from MembershipTerminalServiceAccessEntity o where o.channelServiceAccess.id = :channelServiceAccessId and o.membershipTerminalAccess.membership.customerAccount.account.accountNo = :accountNo and o.membershipTerminalAccess.membership.person.id = :personId")
    List<MembershipTerminalServiceAccessEntity> findByChannelServiceAccessIdAndAccountNoAndPersonId(@Param("channelServiceAccessId") Long channelServiceAccessId, @Param("accountNo") String accountNo,@Param("personId") Integer personId);

}
