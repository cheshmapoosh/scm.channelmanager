package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientVersionRepository extends JpaRepository<ClientVersionEntity,Long> {

    Optional<ClientVersionEntity> findByClient(ClientEntity clientEntity);
    List<ClientVersionEntity> findByClientId(Long clientId);
}
