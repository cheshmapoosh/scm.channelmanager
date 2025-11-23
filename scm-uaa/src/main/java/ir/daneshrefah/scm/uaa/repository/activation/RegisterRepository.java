package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.uaa.repository.activation.domain.RegisterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface RegisterRepository extends JpaRepository<RegisterEntity, Long> {

    @Modifying
    @Query("UPDATE RegisterEntity r SET r.blockedTime = :blockedTime WHERE r.id= :id")
    void updateRegisterBlockedTime(@Param("id") Long id,@Param("blockedTime") ZonedDateTime blockedTime);

    List<RegisterEntity> findTop20ByPhoneNumberOrderByLastRegisterDesc(String phoneNumber);

}
