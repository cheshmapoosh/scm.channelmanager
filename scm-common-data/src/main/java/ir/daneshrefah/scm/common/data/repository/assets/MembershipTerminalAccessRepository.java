package ir.daneshrefah.scm.common.data.repository.assets;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTerminalAccessRepository extends JpaRepository<MembershipTerminalAccessEntity, Long>, JpaSpecificationExecutor<MembershipTerminalAccessEntity> {

    @Query("SELECT m FROM MembershipTerminalAccessEntity m WHERE m.membership.person.id = :personId and m.channel.id = :channelId")
    List<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByPersonId(@Param("personId") Integer personId, @Param("channelId") Short channelId);

    @Query("SELECT m FROM MembershipTerminalAccessEntity m WHERE m.membership.person.id = :personId and m.channel.id = :channelId and m.membership.customerAccount.account.accountNo = :accountNumber")
    Optional<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByPersonIdAndAccountNo(@Param("personId") Integer personId, @Param("channelId") Short channelId,@Param("accountNumber") String accountNumber);

    Optional<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByMembership_IdAndChannelCode(Long membership_Id, String channelCode);

    Optional<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByMembership_Id(Long id);


}
