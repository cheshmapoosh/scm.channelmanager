package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.uaa.repository.activation.domain.PwaLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface UserLoginRepository extends JpaRepository<PwaLoginEntity, Long> {

    @Modifying
    @Query("UPDATE PwaLoginEntity l SET l.blockedTime = ?2 WHERE l.id= ?1")
    void updateLoginBlockedTime(Long id, ZonedDateTime blockedTime);

    List<PwaLoginEntity> findTop10ByUsernameOrderByIdDesc(String username);

}
