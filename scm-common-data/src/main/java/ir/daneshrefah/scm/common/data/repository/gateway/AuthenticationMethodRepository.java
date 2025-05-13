package ir.daneshrefah.scm.common.data.repository.gateway;

import ir.daneshrefah.scm.common.data.entity.gateway.AuthenticationMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationMethodRepository extends JpaRepository<AuthenticationMethodEntity, Integer> {
}
