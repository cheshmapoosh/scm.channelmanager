package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {

    Optional<CustomerEntity> findByCustomerNo(String customerNo);
}
