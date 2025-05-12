package ir.daneshrefah.scm.common.data.repository.assets;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalServiceAccessEntity;
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
            where o.membershipTerminalAccess.legacyTerminal.id = :legacyTerminalId
              and o.membershipTerminalAccess.legacyTerminal.parentId is null
              and o.membershipTerminalAccess.membership.customerAccount.account.accountNo = :accountNo
              and o.membershipTerminalAccess.membership.person.id = :personId
            """)
    List<MembershipTerminalServiceAccessEntity> findByLegacyTerminalIdAndAccountNo(@Param("legacyTerminalId") Integer legacyTerminalId, @Param("accountNo") String accountNo,@Param("personId") Long personId);

    @Query("select o from MembershipTerminalServiceAccessEntity o where o.channelServiceAccess.id = :channelServiceAccessId and o.membershipTerminalAccess.membership.customerAccount.account.accountNo = :accountNo and o.membershipTerminalAccess.membership.person.id = :personId")
    List<MembershipTerminalServiceAccessEntity> findByChannelServiceAccessIdAndAccountNoAndPersonId(@Param("channelServiceAccessId") Long channelServiceAccessId, @Param("accountNo") String accountNo,@Param("personId") Long personId);

}
