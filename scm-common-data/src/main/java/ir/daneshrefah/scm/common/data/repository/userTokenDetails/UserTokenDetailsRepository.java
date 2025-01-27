package ir.daneshrefah.scm.common.data.repository.userTokenDetails;

import ir.daneshrefah.scm.common.data.entity.UserTokenDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenDetailsRepository extends JpaRepository<UserTokenDetails, Long> {

    Optional<UserTokenDetails> findByPersonIdAndTerminalLegacyTerminalId(Long personId, Long TerminalLegacyTerminalId);

    List<UserTokenDetails> findByPersonId(Long personId);

    Optional<UserTokenDetails> findByPersonUsernameAndTerminalLegacyTerminalId(String username, Long TerminalLegacyTerminalId);

    long countByPersonUsernameAndTerminalLegacyTerminalId(String username, Long TerminalLegacyTerminalId);
}
