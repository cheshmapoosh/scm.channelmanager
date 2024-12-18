package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientAuthorizationGrantTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAuthorizationGrantTypeRepository extends JpaRepository<ClientAuthorizationGrantTypeEntity,Long> {

    List<ClientAuthorizationGrantTypeEntity> findByClientId(Long clientId);
    Optional<ClientAuthorizationGrantTypeEntity> findByClientIdAndAuthorizationGrantType(Long clientId, AuthorizationGrantType grantType);


}
