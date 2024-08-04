package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.AccountTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountTypeEntity,Long> {
}
