package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.uaa.repository.activation.domain.WhiteListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhiteListRepository extends JpaRepository<WhiteListEntity, Long> {

    Optional<WhiteListEntity> findByUsername(String username);
}
