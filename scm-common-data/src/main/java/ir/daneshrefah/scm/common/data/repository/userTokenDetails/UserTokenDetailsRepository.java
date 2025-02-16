package ir.daneshrefah.scm.common.data.repository.userTokenDetails;

import ir.daneshrefah.scm.common.data.entity.UserTokenDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenDetailsRepository extends JpaRepository<UserTokenDetails, Long> {
    long countByPersonId(Long personId);
}
