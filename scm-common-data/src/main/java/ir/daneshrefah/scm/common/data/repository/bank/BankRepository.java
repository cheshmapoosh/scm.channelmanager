package ir.daneshrefah.scm.common.data.repository.bank;

import ir.daneshrefah.scm.common.data.entity.bank.BankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<BankEntity, Long>, JpaSpecificationExecutor<BankEntity> {
}