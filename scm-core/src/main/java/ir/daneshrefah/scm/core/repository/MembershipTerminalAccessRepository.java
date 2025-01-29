package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.MembershipTerminalAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTerminalAccessRepository extends JpaRepository<MembershipTerminalAccessEntity, Long>, JpaSpecificationExecutor<MembershipTerminalAccessEntity> {

    @Query("SELECT m FROM MembershipTerminalAccessEntity m WHERE m.membership.person.id = :personId and m.terminal.id = :terminalId")
    List<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByPersonId(@Param("personId") Long personId, @Param("terminalId") String terminalId);

    Optional<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByMembership_IdAndTerminal_Code(Long membership_Id, String terminalCode);

    Optional<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByMembership_Id(Long id);


}
