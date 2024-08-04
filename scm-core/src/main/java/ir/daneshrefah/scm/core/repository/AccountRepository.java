package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity,Long> {

    Optional<AccountEntity> findByAccountNo(String accountNo);
}
