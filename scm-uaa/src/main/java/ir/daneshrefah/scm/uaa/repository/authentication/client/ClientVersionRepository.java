package ir.daneshrefah.scm.uaa.repository.authentication.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientVersionRepository extends JpaRepository<ClientVersionEntity,Long> {
}
