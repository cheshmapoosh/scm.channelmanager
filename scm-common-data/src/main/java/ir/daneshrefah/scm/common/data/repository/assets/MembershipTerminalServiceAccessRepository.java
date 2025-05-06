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
            select o from MembershipTerminalServiceAccessEntity o
                where o.membershipTerminalAccess.terminal.legacyTerminalId = :legacyTerminalId
                    and o.membershipTerminalAccess.membership.customerAccount.account.accountNo = :accountNo
            """)
    List<MembershipTerminalServiceAccessEntity> findByLegacyTerminalIdAndAccountNo(@Param("legacyTerminalId") Integer legacyTerminalId, @Param("accountNo") String accountNo);

}
