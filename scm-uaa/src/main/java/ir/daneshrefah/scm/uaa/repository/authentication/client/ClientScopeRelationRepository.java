package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientScopeRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientScopeRelationRepository extends JpaRepository<ClientScopeRelationEntity, Long> {

    List<ClientScopeRelationEntity> findByClientId(Long clientId);
    Optional<ClientScopeRelationEntity> findByClientIdAndScopeId(Long clientId, Long scopeId);
}
