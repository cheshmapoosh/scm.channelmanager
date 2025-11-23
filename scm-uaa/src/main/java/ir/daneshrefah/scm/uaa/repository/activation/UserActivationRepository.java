package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.uaa.repository.activation.domain.UserActivationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivationRepository extends JpaRepository<UserActivationEntity, Long> {

    Optional<UserActivationEntity> findFirstByUsernameAndPhoneNumberOrderByLastUsedDesc(String username, String phoneNumber);

    List<UserActivationEntity> findAllByUsernameAndPhoneNumberAndRegistryTokenOrderByLastUsedDesc(String username, String phoneNumber,String registryToken);

    @Modifying
    @Query("UPDATE UserActivationEntity c SET c.retryCount = ?2 WHERE c.id= ?1")
    void updateClientRetryCount(Long id, Integer retryCount);

    @Modifying
    @Query("UPDATE UserActivationEntity c SET c.codeValid = ?2 WHERE c.id= ?1")
    void updateClientCodeValidity(Long id, Boolean codeValid);

    @Modifying
    @Query("UPDATE UserActivationEntity c SET c.activated = ?2 WHERE c.id= ?1")
    void updateActivationStatus(Long id, Boolean activated);
}
