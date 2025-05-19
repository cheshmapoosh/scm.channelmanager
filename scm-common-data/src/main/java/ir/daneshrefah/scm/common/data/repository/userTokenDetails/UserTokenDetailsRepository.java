package ir.daneshrefah.scm.common.data.repository.userTokenDetails;

import ir.daneshrefah.scm.common.data.entity.UserTokenDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTokenDetailsRepository extends JpaRepository<UserTokenDetails, Long> {
    long countByPersonId(Integer personId);
}
