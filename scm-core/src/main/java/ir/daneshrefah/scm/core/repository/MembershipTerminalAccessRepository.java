package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.MembershipTerminalAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipTerminalAccessRepository extends JpaRepository<MembershipTerminalAccessEntity, Long>, JpaSpecificationExecutor<MembershipTerminalAccessEntity> {

    @Query("SELECT m FROM MembershipTerminalAccessEntity m WHERE m.membership.person.id = :personId and m.terminal.id = :terminalId")
    Iterable<MembershipTerminalAccessEntity> findMembershipTerminalAccessEntitiesByPersonId(@Param("personId") Long personId,
                                                                                            @Param("terminalId")String terminalId);

}
