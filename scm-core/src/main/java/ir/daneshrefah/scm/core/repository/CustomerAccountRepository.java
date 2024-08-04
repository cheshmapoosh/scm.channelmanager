package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.AccountEntity;
import ir.daneshrefah.scm.core.entity.asset.CustomerAccountEntity;
import ir.daneshrefah.scm.core.entity.asset.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccountEntity,Long> {

    Optional<CustomerAccountEntity> findByCustomerAndAccount(CustomerEntity customerEntity, AccountEntity accountEntity);
}
